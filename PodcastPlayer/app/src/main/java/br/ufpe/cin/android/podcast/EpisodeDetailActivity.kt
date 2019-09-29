package br.ufpe.cin.android.podcast

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_episode_detail.*

class EpisodeDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_episode_detail)

        txt_titulo_episodio.text = intent.getStringExtra("title")
        txt_link_episodio.text = intent.getStringExtra("link")
        txt_descricao_episodio.text = intent.getStringExtra("description")
    }
}
