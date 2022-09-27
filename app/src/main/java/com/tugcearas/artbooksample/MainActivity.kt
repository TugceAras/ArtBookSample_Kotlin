package com.tugcearas.artbooksample

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.tugcearas.artbooksample.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var artList: ArrayList<ArtModel>
    private lateinit var artAdapter : ArtAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        artList = ArrayList<ArtModel>()
        artAdapter = ArtAdapter(artList)
        binding.recyclerView.adapter = artAdapter

        getDataFromDatabase()

    }

    private fun getDataFromDatabase(){

        try {
            val db = this.openOrCreateDatabase("Arts", MODE_PRIVATE,null)
            val cursor = db.rawQuery("SELECT * FROM arts",null)
            val artNameIndex = cursor.getColumnIndex("artName")
            val idIndex = cursor.getColumnIndex("id")
            while (cursor.moveToNext()){
                val name = cursor.getString(artNameIndex)
                val id = cursor.getInt(idIndex)
                val artModel = ArtModel(name,id)
                artList.add(artModel)
            }
            artAdapter.notifyDataSetChanged()
            cursor.close()

        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.art_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == R.id.addArtItem){
            val intent = Intent(this@MainActivity,ArtBookDetailsActivity::class.java)
            intent.putExtra("info","newArt")
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }

}