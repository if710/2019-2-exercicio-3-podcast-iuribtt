package br.ufpe.cin.android.podcast.DAO

import androidx.room.*
import br.ufpe.cin.android.podcast.Model.ItemFeed


@Dao
interface ItemFeedDAO {

    @JvmSuppressWildcards
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun inserirItemFeed(itemFeeds: List<ItemFeed>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun inserirItemFeed(vararg itemFeed: ItemFeed)

    @Update
    fun atualizarItemFeed(vararg itemFeed: ItemFeed)

    @Delete
    fun removerItemFeed(vararg itemFeed: ItemFeed)

    @Query("SELECT * FROM item_feed")
    fun todosItemFeed() : List<ItemFeed>

    @Query("SELECT * FROM item_feed WHERE description LIKE :q")
    fun buscaItemFeedPorDescricao(q : String) : List<ItemFeed>

}