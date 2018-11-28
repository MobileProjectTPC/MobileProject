package com.example.joni.mobileproject

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import java.io.File
import java.io.IOException

class PdfActivity: AppCompatActivity(), View.OnClickListener{

    private var pdfFile: File? = null

    private var fileFromActivity: File? = null
    private val STATE_CURRENT_PAGE_INDEX = "current_page_index"
    private val TAG = "PdfRendererBasicFragment"
    private val INITIAL_PAGE_INDEX = 0
    private lateinit var fileDescriptor: ParcelFileDescriptor
    private lateinit var pdfRenderer: PdfRenderer
    private lateinit var currentPage: PdfRenderer.Page
    private lateinit var imageView: ImageView
    private lateinit var btnPrevious: Button
    private lateinit var btnNext: Button
    private var pageIndex: Int = INITIAL_PAGE_INDEX




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdf)

        fileFromActivity = File(intent.getStringExtra("pdffile"))

        imageView = this.findViewById(R.id.pdfImg)
        btnPrevious = this.findViewById<Button>(R.id.btnPrevious).also { it.setOnClickListener(this) }
        btnNext = this.findViewById<Button>(R.id.btnNext).also { it.setOnClickListener(this)}

        // If there is a savedInstanceState (screen orientations, etc.), we restore the page index.
        if (savedInstanceState != null) {
            pageIndex = savedInstanceState.getInt(STATE_CURRENT_PAGE_INDEX, INITIAL_PAGE_INDEX)
        } else {
            pageIndex = INITIAL_PAGE_INDEX
        }



    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null){
            Log.d("TAG", "Data: $data Data.Data: ${data.data}")

            val dataUri = data.data.path
            pdfFile = File(dataUri)
        }
    }

    override fun onStart() {
        super.onStart()
        try {
            openRenderer(this)
            showPage(pageIndex)

        } catch (e: IOException) {
            Log.d(TAG, e.toString())
        }
    }

    override fun onStop() {
        try {
            closeRenderer()
        } catch (e: IOException) {
            Log.d(TAG, e.toString())
        }
        super.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(STATE_CURRENT_PAGE_INDEX, currentPage.index)
        super.onSaveInstanceState(outState)
    }

    /**
     * Sets up a [PdfRenderer] and related resources.
     */
    @Throws(IOException::class)
    private fun openRenderer(context: Context?) {
        if (context == null) return

        fileDescriptor = ParcelFileDescriptor.open(fileFromActivity, ParcelFileDescriptor.MODE_READ_ONLY)
        // This is the PdfRenderer we use to render the PDF.
        pdfRenderer = PdfRenderer(fileDescriptor)
        currentPage = pdfRenderer.openPage(pageIndex)
    }

    /**
     * Closes the [PdfRenderer] and related resources.
     *
     * @throws IOException When the PDF file cannot be closed.
     */
    @Throws(IOException::class)
    private fun closeRenderer() {
        currentPage.close()
        pdfRenderer.close()
        fileDescriptor.close()
    }

    /**
     * Shows the specified page of PDF to the screen.
     *
     * @param index The page index.
     */
    private fun showPage(index: Int) {
        if (pdfRenderer.pageCount <= index) return

        // Make sure to close the current page before opening another one.
        currentPage.close()
        // Use `openPage` to open a specific page in PDF.
        currentPage = pdfRenderer.openPage(index)
        // Important: the destination bitmap must be ARGB (not RGB).
        val bitmap = Bitmap.createBitmap(currentPage.width, currentPage.height, Bitmap.Config.ARGB_8888)
        // Here, we render the page onto the Bitmap.
        // To render a portion of the page, use the second and third parameter. Pass nulls to get
        // the default result.
        // Pass either RENDER_MODE_FOR_DISPLAY or RENDER_MODE_FOR_PRINT for the last parameter.
        currentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        // We are ready to show the Bitmap to user.
        imageView.setImageBitmap(bitmap)
        updateUi()
    }

    /**
     * Updates the state of 2 control buttons in response to the current page index.
     */
    private fun updateUi() {
        val index = currentPage.index
        val pageCount = pdfRenderer.pageCount
        btnPrevious.isEnabled = (0 != index)
        btnNext.isEnabled = (index + 1 < pageCount)
        this.title = getString(R.string.app_name_with_index, index + 1, pageCount)
    }

    /**
     * Returns the page count of of the PDF.
     */
    fun getPageCount() = pdfRenderer.pageCount

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btnPrevious -> {
                // Move to the previous page/
                showPage(currentPage.index - 1)
            }
            R.id.btnNext -> {
                // Move to the next page.
                showPage(currentPage.index + 1)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (fileFromActivity != null){
            fileFromActivity!!.delete()
        }
        Log.d("PdfActivity", "Destroyed")
    }

}