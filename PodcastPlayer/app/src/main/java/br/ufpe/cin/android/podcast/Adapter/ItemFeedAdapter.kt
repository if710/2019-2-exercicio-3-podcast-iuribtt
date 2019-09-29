package br.ufpe.cin.android.podcast.Adapter

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import br.ufpe.cin.android.podcast.EpisodeDetailActivity
import br.ufpe.cin.android.podcast.Model.ItemFeed
import br.ufpe.cin.android.podcast.R
import br.ufpe.cin.android.podcast.Suporte.DownloadService
import br.ufpe.cin.android.podcast.Suporte.MusicPlayerService
import kotlinx.android.synthetic.main.itemlista.view.*
import java.io.File

class ItemFeedAdapter(private val myDataset: List<ItemFeed>) :
    RecyclerView.Adapter<ItemFeedAdapter.MyViewHolder>() {

    private lateinit var context: Context
    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {
        // create a new view
        val textView = LayoutInflater.from(parent.context)
            .inflate(R.layout.itemlista, parent, false)
        // set the view's size, margins, paddings and layout parameters

        context = parent.getContext();

        return MyViewHolder(textView)
    }

    internal var musicPlayerService: MusicPlayerService? = null
    internal var isBound = false
    internal val TAG = "MusicBindingActivity"

    private val sConn = object : ServiceConnection {
        override fun onServiceDisconnected(p0: ComponentName?) {
            musicPlayerService = null
            isBound = false
        }

        override fun onServiceConnected(p0: ComponentName?, b: IBinder?) {
            val binder = b as MusicPlayerService.MusicBinder
            musicPlayerService = binder.service
            isBound = true

        }

    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val itemFeed = myDataset[position]
        holder.title?.text = itemFeed.title
        holder.title.setOnClickListener {

            val intent = Intent(it.context, EpisodeDetailActivity::class.java)
            intent.putExtra("title", itemFeed.title)
            intent.putExtra("description", itemFeed.description)
            intent.putExtra("link", itemFeed.link)
            intent.putExtra("pubDate", itemFeed.pubDate)
            intent.putExtra("downloadLink", itemFeed.downloadLink)
            it.context.startActivity(intent)
        }

        //holder.action?.text = itemFeed.
        holder.data?.text = itemFeed.pubDate

        val root = context.getExternalFilesDir(context.getString(R.string.app_name))

        val output = File(root, Uri.parse(itemFeed.downloadLink).lastPathSegment)

        var isDownload = true
        if (!output.exists()) {
            holder.btnAction.setBackgroundResource(R.drawable.ic_download_outline)
            isDownload = true;
        } else {

            val serviceIntent = Intent(context, MusicPlayerService::class.java)

//            if(serviceIntent.)
            holder.btnAction.setBackgroundResource(R.drawable.ic_play_circle)
            isDownload = false;
        }

        holder.btnAction.setOnClickListener{
            if(isDownload) {
                val downloadService = Intent(it.context, DownloadService::class.java)
                downloadService.data = Uri.parse(itemFeed.downloadLink)
                downloadService.putExtra("title", itemFeed.title)
                downloadService.putExtra("link", itemFeed.link)
                it.context.startService(downloadService)
            }else{

                if (!isBound) {
                    Toast.makeText(context, "Fazendo o Binding...", Toast.LENGTH_SHORT).show()
                    val bindIntent = Intent(context, MusicPlayerService::class.java)
                    isBound = context.bindService(bindIntent,sConn, Context.BIND_AUTO_CREATE)
                }

                val musicServiceIntent = Intent(context, MusicPlayerService::class.java)
                context.startService(musicServiceIntent)

                val musicPlayerServiceIntent = Intent(it.context, MusicPlayerService::class.java)
                musicPlayerServiceIntent.data = Uri.parse(itemFeed.downloadLink)
                it.context.startService(musicPlayerServiceIntent)

                if (isBound) {
                    musicPlayerService?.playMusic()
                }

            }
        }

    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = myDataset.size

    class MyViewHolder(item: View) : RecyclerView.ViewHolder(item), View.OnClickListener {
        val title = item.item_title
        val data = item.item_date
        val btnAction = item.item_action

        init {
            item.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            val position = adapterPosition
            Toast.makeText(v.context, "Clicou no item da posição: $position", Toast.LENGTH_SHORT)
                .show()
        }
    }
}