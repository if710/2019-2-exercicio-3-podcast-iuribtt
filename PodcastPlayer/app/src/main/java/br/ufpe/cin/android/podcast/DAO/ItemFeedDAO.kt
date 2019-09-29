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

    /**
     * Atualizar apenas o caminho do arquivo MP3 baixado
     */
    @Query("UPDATE item_feed SET pathFilePodCastMP3=:pathFilePodCastMP3 WHERE link = :link")
    fun atualizarPathMP3ItemFeed(link: String?, pathFilePodCastMP3: String)

    @Update
    fun atualizarItemFeed(vararg itemFeed: ItemFeed)

    @Delete
    fun removerItemFeed(vararg itemFeed: ItemFeed)

    @Query("SELECT * FROM item_feed")
    fun todosItemFeed() : List<ItemFeed>

    @Query("SELECT * FROM item_feed WHERE description LIKE :q")
    fun buscaItemFeedPorDescricao(q : String) : List<ItemFeed>

}