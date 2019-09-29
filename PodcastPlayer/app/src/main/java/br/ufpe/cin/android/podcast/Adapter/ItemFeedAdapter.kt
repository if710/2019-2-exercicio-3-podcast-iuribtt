package br.ufpe.cin.android.podcast.Adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import br.ufpe.cin.android.podcast.EpisodeDetailActivity
import br.ufpe.cin.android.podcast.Model.ItemFeed
import br.ufpe.cin.android.podcast.R
import kotlinx.android.synthetic.main.itemlista.view.*

class ItemFeedAdapter(private val myDataset: List<ItemFeed>) :
    RecyclerView.Adapter<ItemFeedAdapter.MyViewHolder>() {

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {
        // create a new view
        val textView = LayoutInflater.from(parent.context)
            .inflate(R.layout.itemlista, parent, false)
        // set the view's size, margins, paddings and layout parameters

        return MyViewHolder(textView)
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

        //holder.action?.text = itemFeed.
        holder.btnAction.setOnClickListener{

            Toast.makeText(it.context, "Clicou no item da posição: ${itemFeed.downloadLink}", Toast.LENGTH_SHORT)
                .show()
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