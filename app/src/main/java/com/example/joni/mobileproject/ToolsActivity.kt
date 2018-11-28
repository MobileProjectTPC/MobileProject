package com.example.joni.mobileproject

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.*
import android.support.v7.app.AppCompatActivity
import android.transition.ChangeBounds
import android.transition.ChangeClipBounds
import android.transition.ChangeTransform
import android.transition.TransitionSet
import android.util.Log
import android.view.View
import com.example.joni.mobileproject.adapters.TransitionNavigation
import com.example.joni.mobileproject.fragments.DetailFragment
import com.example.joni.mobileproject.Image
import com.example.joni.mobileproject.fragments.ToolFragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.home_fragment_layout.*
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import android.os.Bundle
import android.widget.Toast
import com.google.android.gms.common.util.IOUtils.toByteArray
import java.io.ByteArrayOutputStream


class ToolsActivity : AppCompatActivity(), TransitionNavigation {

    private val firebaseDatabase = FirebaseDatabase.getInstance()

    val newlist = java.util.ArrayList<Image>()

    val bitmapList = java.util.ArrayList<ByteArray>()

    lateinit var detailFragment: DetailFragment

    var tool: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tools)

        getStuffFromFirebaseDB("workspace", "tool1", "images")

        tool = if (savedInstanceState == null) {
            val extras = intent.extras
            extras?.getString("Tool")
        } else {
            //savedInstanceState.getSerializable("Tool") as String
            savedInstanceState.getString("Tool")
        }

        Log.d("tää", "tool??? = $tool")

        /*
        supportFragmentManager.beginTransaction()
                .replace(R.id.root, ToolFragment())
                .commitAllowingStateLoss()
                */

    }

    fun taaniivitusti(tool: String) {
        var position = 0
        var page = 0
        Log.d("tää", "tool!!!!!! = $tool")

        if (tool == "tool1") {
            position = 0
            page = 0
            Log.d("tää", "tool1 = $tool")
        }
        else if (tool == "tool2") {
            position = 1
            page = 1
            Log.d("tää", "tool2 = $tool")
        }
        else {
            position = 2
            page = 2
            Log.d("tää", "tool3 = $tool")
        }

        val transaction = supportFragmentManager.beginTransaction()



        detailFragment = DetailFragment.newInstance(position, page, newlist)


        transaction.replace(R.id.root, detailFragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    override fun goToDetail(transitionItems: List<View>, position: Int, page: Int) {


        val transaction = supportFragmentManager.beginTransaction()

        transitionItems.forEach {
            transaction.addSharedElement(it, it.transitionName)
        }

        detailFragment = DetailFragment.newInstance(position, page, newlist)

        val transitionSet = TransitionSet().apply {
            addTransition(ChangeTransform())
            addTransition(ChangeClipBounds())
            addTransition(ChangeBounds())
        }

        detailFragment.sharedElementEnterTransition = transitionSet
        detailFragment.sharedElementReturnTransition = transitionSet

        transaction.replace(R.id.root, detailFragment)
        transaction.addToBackStack(null)
        transaction.commit()


    }


    inner class GetCont(): AsyncTask<URL, Unit, Bitmap>() {


        override fun doInBackground(vararg url: URL?): Bitmap {
            lateinit var bm: Bitmap
            try {
                val myConn = url[0]!!.openConnection() as HttpURLConnection
                val iStream: InputStream = myConn.inputStream
                bm = BitmapFactory.decodeStream(iStream)
                myConn.disconnect()
            } catch (e:Exception) {
                Log.e("Connection", "Reading error", e)
            }
            return bm
        }

        override fun onPostExecute(result: Bitmap) {

            val stream = ByteArrayOutputStream()
            result.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val byteArray = stream.toByteArray()
            bitmapList.add(byteArray)


            Log.d("tää", "$bitmapList")
            /*
            val stream = ByteArrayOutputStream()
            result.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val byteArray = stream.toByteArray()

            val b = Bundle()
            b.putByteArray("image", byteArray)
            */




        }
    }



    private fun getStuffFromFirebaseDB(workspace: String, tool: String, dataType: String) {
        val ref = firebaseDatabase.getReference("/$workspace/tools/$tool/$dataType")
        //showLoadingDialog("Loading image")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(p0: DataSnapshot) {

                p0.children.forEach {
                    Log.d("HomeFragment", "Stuff: $it")
                    newlist.add(it.getValue(Image::class.java)!!)
                    Log.d("tää", "tää ois tää $newlist")
                    //GetCont().execute(URL(it.getValue(Image::class.java)!!.imageUrl))

                }

                if (tool != null) {
                    taaniivitusti(tool)
                }
                else {
                    val mfc = ToolFragment()
                    val b = Bundle()
                    b.putSerializable("Parcel", newlist)
                    mfc.arguments = b

                    supportFragmentManager.beginTransaction()
                            .add(R.id.root, mfc)
                            .commitAllowingStateLoss()
                }



            }

            override fun onCancelled(p0: DatabaseError) {
            }
        })
    }

}
