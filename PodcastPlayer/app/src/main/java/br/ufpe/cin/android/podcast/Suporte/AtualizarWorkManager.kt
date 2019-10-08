package br.ufpe.cin.android.podcast.Suporte

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import br.ufpe.cin.android.podcast.DAO.ItemFeedDB
import br.ufpe.cin.android.podcast.MainActivity
import br.ufpe.cin.android.podcast.Model.ItemFeed
import br.ufpe.cin.android.podcast.Util.Parser
import org.jetbrains.anko.doAsync
import org.xmlpull.v1.XmlPullParserException
import java.io.BufferedReader
import java.io.IOException
import java.net.URL

class AtualizarWorkManager(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {

    override fun doWork(): Result {

        var bancoDeDados = ItemFeedDB.getDatabase(applicationContext)

        var mFeedModelList: List<ItemFeed>? = null

        return try {

            var urlLink = MainActivity.urlLink

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

            doAsync {
                bancoDeDados.itemFeedDAO().inserirItemFeed(mFeedModelList)
            }

            Result.success()

        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("AtualizarWorkManager", "Error", e)
            Result.failure()
        } catch (e: XmlPullParserException) {
            e.printStackTrace()
            Log.e("AtualizarWorkManager", "Error", e)
            Result.failure()
        }

    }
}