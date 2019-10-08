package br.ufpe.cin.android.podcast.Suporte

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import br.ufpe.cin.android.podcast.DAO.ItemFeedDB
import br.ufpe.cin.android.podcast.MainActivity
import br.ufpe.cin.android.podcast.R
import java.io.File


class MusicPlayerService : Service() {
    private val TAG = "MusicPlayerWithBindingService"
    private var mPlayer: MediaPlayer? = null
    public var isPlaying: Boolean = false
    private val mStartID: Int = 0
    lateinit var link: String
    private val mBinder = MusicBinder()
    private lateinit var bancoDeDados: ItemFeedDB

    override fun onCreate() {
        bancoDeDados = ItemFeedDB.getDatabase(baseContext)
        super.onCreate()
    }

    override fun onStartCommand(i: Intent, flags: Int, startId: Int): Int {
        link = i!!.data.toString()

        val root = getExternalFilesDir(getString(R.string.app_name))
        val output = File(root, Uri.parse(link).lastPathSegment)

        // configurar media player
        mPlayer = MediaPlayer.create(this, Uri.parse(output.absolutePath))

        //fica em loop
        mPlayer?.isLooping = true

        createChannel()
        // cria notificacao na area de notificacoes para usuario voltar p/ Activity
        val notificationIntent = Intent(applicationContext, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)

        val notification = NotificationCompat.Builder(
            applicationContext, CHANNEL_ID
        )
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setOngoing(true).setContentTitle("Music Service rodando")
            .setContentText("Clique para acessar o player!")
            .setAutoCancel(true)
            .setContentIntent(pendingIntent).build()

        // inicia em estado foreground, para ter prioridade na memoria
        // evita que seja facilmente eliminado pelo sistema
        startForeground(NOTIFICATION_ID, notification)


        return Service.START_STICKY
    }

    override fun onDestroy() {
        mPlayer?.release()
        super.onDestroy()
    }

    fun playMusic(startAt: Int) {
        if (!mPlayer!!.isPlaying) {
            //verifica se já foi tocado toda o podcast
            if (mPlayer!!.currentPosition >= startAt) {
                mPlayer!!.seekTo(0);

                //Envia um LocalBroadcast para a aplicação informando que o podcast terminou
                LocalBroadcastManager.getInstance(this).sendBroadcast(Intent(PODCAST_TERMINOU))
                val root = getExternalFilesDir(getString(R.string.app_name))

                //remover o arquivo do dispositivo
                val output = File(root, Uri.parse(link).lastPathSegment)
                if (output.exists()) {
                    output.delete()
                }
            } else {
                mPlayer!!.seekTo(startAt);
            }
            isPlaying = true;
            mPlayer?.start()

        }
    }

    fun pauseMusic() {
        if (mPlayer!!.isPlaying) {
            isPlaying = false;
            mPlayer?.pause()
            bancoDeDados.itemFeedDAO().atualizarQuandoFoiPausado(link, mPlayer!!.currentPosition);
        }
    }

    inner class MusicBinder : Binder() {
        internal val service: MusicPlayerService
            get() = this@MusicPlayerService
    }

    override fun onBind(intent: Intent): IBinder {
        return mBinder
    }

    fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel
            val mChannel = NotificationChannel(
                CHANNEL_ID,
                "Canal de Notificacoes",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            mChannel.description = "Descricao"
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)
        }
    }

    companion object {
        val PODCAST_TERMINOU = "br.ufpe.cin.android.podcast.action.PODCAST_TERMINOU"
        private val NOTIFICATION_ID = 2
        private val CHANNEL_ID = "22"
    }

}
