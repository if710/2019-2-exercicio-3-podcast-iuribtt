package br.ufpe.cin.android.podcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import br.ufpe.cin.android.podcast.Adapter.ItemFeedAdapter
import br.ufpe.cin.android.podcast.DAO.ItemFeedDB
import br.ufpe.cin.android.podcast.Model.ItemFeed
import br.ufpe.cin.android.podcast.Suporte.AtualizarWorkManager
import br.ufpe.cin.android.podcast.Suporte.DownloadService
import br.ufpe.cin.android.podcast.Suporte.MusicPlayerService
import br.ufpe.cin.android.podcast.Util.Parser
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.xmlpull.v1.XmlPullParserException
import java.io.BufferedReader
import java.io.IOException
import java.net.URL
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {

    private lateinit var bancoDeDados: ItemFeedDB
    private var mFeedModelList: List<ItemFeed>? = null

    private var mSwipeLayout: SwipeRefreshLayout? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.new_game -> {
                startActivity(Intent(applicationContext, SettingsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bancoDeDados = ItemFeedDB.getDatabase(baseContext)

        mSwipeLayout = findViewById(R.id.mSwipeLayout)
        mSwipeLayout!!.setOnRefreshListener { FetchFeedTask().execute(null as Void?) }

        viewManager = LinearLayoutManager(this)
        viewAdapter = ItemFeedAdapter(listOf<ItemFeed>())

        recyclerView = findViewById(R.id.my_recycler_view)

        recyclerView = recyclerView.apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(false)

            // use a linear layout manager
            layoutManager = viewManager

            // specify an viewAdapter (see also next example)
            adapter = viewAdapter

        }

        //Carregar Feed
        FetchFeedTask().execute(null as Void?)

        LocalBroadcastManager.getInstance(this).registerReceiver(
            mMessageReceiver,
            IntentFilter(DownloadService.DOWNLOAD_COMPLETE)
        );

        LocalBroadcastManager.getInstance(this).registerReceiver(
            mMessageReceiver2,
            IntentFilter(MusicPlayerService.PODCAST_TERMINOU)
        );


        //Obter o dados salvo pelo usuario no shared preference
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val value = sharedPref.getString(getString(R.string.preference_atualizar_seek_bar), "24")
            ?.toLong()

        //Definir o work manager para atualizar o feed periodicamente
        val workManager = WorkManager.getInstance(application)

        val request = PeriodicWorkRequest.Builder(AtualizarWorkManager::class.java, value!!, TimeUnit.DAYS).build()
        WorkManager.getInstance(baseContext)
            .enqueueUniquePeriodicWork("WorkManagerAtualizarFeed", ExistingPeriodicWorkPolicy.REPLACE, request)

        workManager.enqueue(request)
    }

    private val mMessageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // Get extra data included in the Intent
            val link = intent.getStringExtra("link")
            Log.d("receiver", "Baixou o podcast desse link: " + link!!)

            recyclerView.invalidate()
        }
    }

    private val mMessageReceiver2 = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // Get extra data included in the Intent
            val link = intent.getStringExtra("link")
            Log.d("receiver", "Baixou o podcast desse link: " + link!!)

            recyclerView.invalidate()
        }
    }

    override fun onDestroy() {
        // Unregister since the activity is about to be closed.
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver)
        super.onDestroy()
    }

    public inner class FetchFeedTask : AsyncTask<Void, Void, Boolean>() {



        override fun onPreExecute() {
            mSwipeLayout!!.isRefreshing = true


        }

        override fun doInBackground(vararg voids: Void): Boolean? {
            if (TextUtils.isEmpty(urlLink))
                return false

            try {
                if (!urlLink!!.startsWith("http://") && !urlLink!!.startsWith("https://"))
                    urlLink = "http://" + urlLink!!

                val url = URL(urlLink)
                val inputStream = url.openConnection().getInputStream()

                val reader = BufferedReader(inputStream.reader())
                val content: String
                try {
                    content = reader.readText()
                } finally {
                    reader.close()
                }

                mFeedModelList = Parser.parse(content)
                return true
            } catch (e: IOException) {
                e.printStackTrace()
                Log.e(TAG, "Error", e)
            } catch (e: XmlPullParserException) {
                e.printStackTrace()
                Log.e(TAG, "Error", e)
            }

            return false
        }

        override fun onPostExecute(success: Boolean) {
            mSwipeLayout!!.isRefreshing = false

            if (success) {

                viewAdapter = mFeedModelList?.let {
                    ItemFeedAdapter(
                        it
                    )
                }!!
                recyclerView.setAdapter(mFeedModelList?.let {
                    ItemFeedAdapter(
                        it
                    )
                });

                inserirItensFeedNoBanco(mFeedModelList!!)

            } else {

                //Informar sobre o não carregamento do feed
                Snackbar.make(
                    root_layout,
                    "Não pode carregar o Feed. Tente novamente mais tarde.",
                    Snackbar.LENGTH_SHORT
                ).show()

                carregarFeedDaBase()

            }
        }
    }

    /**
     * insere os itens no banco de dados local
     */
    private fun inserirItensFeedNoBanco(itemFeeds: List<ItemFeed>) {
        doAsync {
            bancoDeDados.itemFeedDAO().inserirItemFeed(itemFeeds)
        }
    }

    /**
     * Carrega os dados do feed
     */
    private fun carregarFeedDaBase() {

        doAsync {
            val itemFeeds = bancoDeDados.itemFeedDAO().todosItemFeed()
            uiThread {
                viewAdapter = itemFeeds?.let {
                    ItemFeedAdapter(
                        it
                    )
                }!!
                recyclerView.setAdapter(itemFeeds?.let {
                    ItemFeedAdapter(
                        it
                    )
                });
            }
        }
    }

    inline fun Context.toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        var urlLink = "https://devnaestrada.com.br/feed.xml"
        private val TAG = "MainActivity"
    }
}
