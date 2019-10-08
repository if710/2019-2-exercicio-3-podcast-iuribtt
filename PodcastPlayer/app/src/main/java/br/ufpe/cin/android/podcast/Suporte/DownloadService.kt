package br.ufpe.cin.android.podcast.Suporte

//import android.os.Environment.DIRECTORY_DOWNLOADS

import android.app.*
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import br.ufpe.cin.android.podcast.DAO.ItemFeedDB
import br.ufpe.cin.android.podcast.MainActivity
import br.ufpe.cin.android.podcast.R
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL




class DownloadService : IntentService("DownloadService") {


    private lateinit var bancoDeDados: ItemFeedDB

    public override fun onHandleIntent(i: Intent?) {

        bancoDeDados = ItemFeedDB.getDatabase(baseContext)

        var notification: Notification?

        try {

            val title = i?.getStringExtra("title")
            val link = i?.getStringExtra("link")

            createChannel()

            val notificationIntent = Intent(applicationContext, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)

            notification = NotificationCompat.Builder(
                applicationContext, "1"
            )
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setOngoing(true).setContentTitle(getString(R.string.app_name))
                .setContentText("Baixando podcast!: \"$title\"")
                .setAutoCancel(true)
                .setContentIntent(pendingIntent).build()

            // inicia em estado foreground, para ter prioridade na memoria
            // evita que seja facilmente eliminado pelo sistema
            startForeground(NOTIFICATION_ID, notification)

            //checar se tem permissao... Android 6.0+
            val root = getExternalFilesDir(getString(R.string.app_name))
            root?.mkdirs()
            val output = File(root, i!!.data!!.lastPathSegment)
            if (output.exists()) {
                output.delete()
            }
            val url = URL(i.data!!.toString())
            val c = url.openConnection() as HttpURLConnection
            val fos = FileOutputStream(output.path)
            val out = BufferedOutputStream(fos)
            try {
                val `in` = c.inputStream
                val buffer = ByteArray(8192)
                var len = `in`.read(buffer)
                while (len >= 0) {
                    out.write(buffer, 0, len)
                    len = `in`.read(buffer)
                }
                out.flush()

                bancoDeDados.itemFeedDAO().atualizarPathMP3ItemFeed(link, output.absolutePath)

            } finally {
                fos.fd.sync()
                out.close()
                c.disconnect()
            }

            notification = NotificationCompat.Builder(
                applicationContext, "1"
            )
                .setSmallIcon(android.R.drawable.arrow_down_float)
                .setOngoing(true).setContentTitle(getString(R.string.app_name))
                .setContentText("Baixado!: \"$title\"")
                .setAutoCancel(true)
                .setContentIntent(pendingIntent).build()

            val intent = Intent(DOWNLOAD_COMPLETE)
            // Passa o link para saber qual foi o podcast baixado
            intent.putExtra("link", link)
            //Envia um LocalBroadcast para a aplicação informando que o download foi concluido
            LocalBroadcastManager.getInstance(this).sendBroadcast(Intent(DOWNLOAD_COMPLETE))

        } catch (e2: IOException) {
            Toast.makeText(
                applicationContext,
                "Erro ao efeutar o download, por favor tente outra vez.", Toast.LENGTH_SHORT
            ).show()
            Log.e(javaClass.getName(), "Exception durante download", e2)
        }

    }

    fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel
            val mChannel = NotificationChannel(
                "1",
                "Canal de Notificacões",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            mChannel.description = "Descricao"
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager =
                getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)
        }
    }

    companion object {
        val DOWNLOAD_COMPLETE = "br.ufpe.cin.android.podcast.action.DOWNLOAD_COMPLETE"
        private val NOTIFICATION_ID = 55
    }
}
