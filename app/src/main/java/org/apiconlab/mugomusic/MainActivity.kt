package org.apiconlab.mugomusic

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.apiconlab.mugomusic.adapters.HistoryAdapter
import org.apiconlab.mugomusic.adapters.SearchAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class MainActivity : AppCompatActivity(), SearchAdapter.OnItemClickListener, HistoryAdapter.OnItemClickListener {

    private var history = ArrayList<SearchDataItem>()
    val temp = ArrayList<SearchDataItem>()
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var seekBar: SeekBar


    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("ClickableViewAccessibility")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // MediaPlayer is initialized
        mediaPlayer = MediaPlayer()

        if(mediaPlayer.isPlaying){
            mediaPlayer.stop()
            mediaPlayer.reset()
        }

        // Action Bar is hide
        supportActionBar?.hide()
        setContentView(R.layout.activity_main)

        // History is loaded
        loadData()
        loadOnResume()

        // Search->MyFunction->MyFunction2->Search Results are displayed
        val search = findViewById<EditText>(R.id.et1)
        doThis(search)


        // What happens when seekbar is manually changed
        seekBar = findViewById(R.id.sb)
        seekBar.setOnTouchListener { v, event ->
            val seekBar = v as SeekBar
            val playPosition = (mediaPlayer.duration / 1000) * seekBar.progress
            mediaPlayer.seekTo(playPosition)
            false
        }

        // What happens to Play/Pause Image Button
        val p: ImageView = findViewById(R.id.p)
        p.setOnClickListener {
            if(mediaPlayer.isPlaying){
                p.setImageResource(R.drawable.ic_play)
                mediaPlayer.pause()
            } else {
                p.setImageResource(R.drawable.ic_pause)
                mediaPlayer.start()
                abc()
            }
        }
    }

    //  //  //  //  //  //  //  //  //  //  Functions //    //  //  //  //  //  //  //  //  //  //

    // 1. doThis: Search->myFunction, Keyboard is collapsed

    private fun doThis(search: EditText){
        search.setOnEditorActionListener(TextView.OnEditorActionListener{ v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                temp.clear()
                myFunction(search.text.toString())
                val k = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                k.hideSoftInputFromWindow(search.windowToken, 0)
                search.isFocusableInTouchMode = false
                search.clearFocus()
                search.isFocusableInTouchMode = true
                return@OnEditorActionListener true
            }
            false
        })
    }

    // 2. myFunction: Search is passed via GET from Interface, result is collected, myFunction2 is
    // called
    private fun myFunction(mQuery: String){
        val re = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://saavn.me/")
            .build()
            .create(MyInterface::class.java)
        val rx = re.getMyData(mQuery)
        rx.enqueue(object : Callback<List<SearchDataItem>> {
            override fun onResponse(
                call: Call<List<SearchDataItem>>,
                response: Response<List<SearchDataItem>>
            ) {
                val body = response.body()!!
                for(data in body){
                    val k = SearchDataItem(data.song_name, data.album_name, data.song_artist, data.song_image, data.download_links)
                    temp.add(k)
                }
                myFunction2(temp)

            }
            override fun onFailure(call: Call<List<SearchDataItem>>, t: Throwable) {
                Log.e("MainActivity","onFailure"+t.message)
            }
        })
    }

    // 3. myFunction2: Assembling data inside the object, creates a list of such objects, sends it
    // to search adapter to display it on the recyclerView
    private fun myFunction2(temp: ArrayList<SearchDataItem>) {
        val rv = findViewById<RecyclerView>(R.id.rv1)
        rv.layoutManager = LinearLayoutManager(this)
        val mAdapter = SearchAdapter(temp, this)
        rv.adapter = mAdapter
    }

    // [Search Results] // 4. What happens when an item from the above recyclerView is clicked,
    // calls prepareSearchMediaPlayer to load data
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onSearchItemClick(position: Int) {
        mediaPlayer.stop()
        mediaPlayer.reset()

        prepareSearchMediaPlayer(position)
        mediaPlayer.start()

        abc()

        saveData(temp[position])
        loadData()
    }

    // [Search Results] // 5. When the item is clicked, this loads data from the recyclerView into
    // the mediaPlayer
    @SuppressLint("SetTextI18n")
    private fun prepareSearchMediaPlayer(position: Int){
        val str = temp[position].download_links[0]
        val playerSongImg: ImageView = findViewById(R.id.p_song_img)
        val playerSongName: TextView = findViewById(R.id.p_song_name)
        val playerSongDes: TextView = findViewById(R.id.p_song_des)
        val p: ImageView = findViewById(R.id.p)
        try{
            mediaPlayer.setDataSource(str)
            mediaPlayer.prepare()
            Glide.with(this).load(temp[position].song_image).into(playerSongImg)
            playerSongName.text = temp[position].song_name
            playerSongDes.text = temp[position].album_name + " " + temp[position].song_artist
            p.setImageResource(R.drawable.ic_pause)
        } catch(e:Exception) {
            Toast.makeText(applicationContext, e.message, Toast.LENGTH_LONG).show()
        }
    }

    // [History Results] // 6. What happens when an item from the history recyclerView is clicked,
    // calls prepareHistoryMediaPlayer to load data

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onHistoryItemClick(position: Int) {
        mediaPlayer.stop()
        mediaPlayer.reset()

        prepareHistoryMediaPlayer(position)
        mediaPlayer.start()

        abc()
        saveData(history[position])
        loadData()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun abc(){
        seekBar.max = 1000
        Thread {
            var progress = 0.00
            while (mediaPlayer.isPlaying) {
                runOnUiThread {
                    progress = (mediaPlayer.currentPosition.toDouble() * 1000) / mediaPlayer.duration
                    seekBar.setProgress(progress.toInt(), true)
                }
                Thread.sleep(1000)
            }
            val p: ImageView = findViewById(R.id.p)
            if(!mediaPlayer.isPlaying){
                p.setImageResource(R.drawable.ic_play)
                seekBar.setProgress(0, true)
            }
        }.start()

    }

    // [History Results] // 7. When the item is clicked, this loads data from the recyclerView into
    // the mediaPlayer
    @SuppressLint("SetTextI18n")
    private fun prepareHistoryMediaPlayer(position: Int){
        val str = history[position].download_links[0]
        val playerSongImg: ImageView = findViewById(R.id.p_song_img)
        val playerSongName: TextView = findViewById(R.id.p_song_name)
        val playerSongDes: TextView = findViewById(R.id.p_song_des)
        val p: ImageView = findViewById(R.id.p)
        try{
            mediaPlayer.setDataSource(str)
            mediaPlayer.prepare()
            Glide.with(this).load(history[position].song_image).into(playerSongImg)
            playerSongName.text = history[position].song_name
            playerSongDes.text = history[position].album_name + " " + history[position].song_artist
            p.setImageResource(R.drawable.ic_pause)
        } catch(e:Exception) {
            Toast.makeText(applicationContext, "Exception", Toast.LENGTH_LONG).show()
        }
    }

    // [History Results] // 8. Storing history in Shared Preferences
    private fun saveData(mObject: SearchDataItem){
        val sp = getSharedPreferences("mugosp", MODE_PRIVATE)
        val editor = sp.edit()
        val gson = Gson()
        history.add(mObject)
        val json = gson.toJson(history)
        editor.putString("recent items", json)
        editor.apply()
    }

    // [History Results] // 9. Load history from Shared Preferences
    private fun loadData(){
        val sp = getSharedPreferences("mugosp", MODE_PRIVATE)
        val gson = Gson()
        val json = sp.getString("recent items", null)!!
        if(json != null) {
            val type = object : TypeToken<ArrayList<SearchDataItem>>() {}.type
            history = gson.fromJson(json, type)

            val rv = findViewById<RecyclerView>(R.id.rv2)
            rv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, true)
            rv.scrollToPosition(history.size-1)
            val mAdapter = HistoryAdapter(history, this)
            rv.adapter = mAdapter
        }
    }

    override fun onBackPressed() {
        /*
        val startMain = Intent(Intent.ACTION_MAIN)
        startMain.addCategory(Intent.CATEGORY_APP_CALCULATOR)
        startMain.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(startMain)
         */

        this.moveTaskToBack(true)

    }

    private fun loadOnResume(){
        val k = history.size - 1
        val str = history[k].download_links[0]
        val playerSongImg: ImageView = findViewById(R.id.p_song_img)
        val playerSongName: TextView = findViewById(R.id.p_song_name)
        val playerSongDes: TextView = findViewById(R.id.p_song_des)
        val p: ImageView = findViewById(R.id.p)
        try{
            mediaPlayer.setDataSource(str)
            mediaPlayer.prepare()
            Glide.with(this).load(history[k].song_image).into(playerSongImg)
            playerSongName.text = history[k].song_name
            playerSongDes.text = history[k].album_name + " " + history[k].song_artist
        } catch(e:Exception) {
            Toast.makeText(applicationContext, "Exception", Toast.LENGTH_LONG).show()
        }
    }

}