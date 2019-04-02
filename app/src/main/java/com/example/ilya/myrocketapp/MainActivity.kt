package com.example.ilya.myrocketapp

import android.graphics.Bitmap
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Spinner

import java.util.ArrayList
import java.util.Random
import java.util.concurrent.TimeUnit

import rx.Observable
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

class MainActivity : AppCompatActivity() {

    private var genButton: Button? = null
    private var sizeUp: Button? = null
    private var viewGroupHolder: LinearLayout? = null
    private var sizeDown: Button? = null
    private var speedBar: SeekBar? = null
    private var qrCode: ImageView? = null
    private var qrCode2: ImageView? = null
    private var qrSetting1: Spinner? = null
    private var qrSetting2: Spinner? = null
    private var currentBitmap: Bitmap? = null
    private var currentBitmap2: Bitmap? = null

    private var pixelCountInSquare = 25
    private val MAX_SQUARE_VALUE = 200
    private val MIN_SQUARE_VALUE = 25

    // MY ALGORITHM
    private var arrBoolean: Array<BooleanArray> = Array(1) { BooleanArray(1) }// состояния пикселей битмапки (чтобы не вычитывать пожертвуем памятью) Массив boolean можем себе позволить
    private var subscrOnUpdate: Subscription? = null // подписка на апдейт состояния view
    private var subscrOnUpdate2: Subscription? = null // подписка на апдейт состояния view
    private var fillTaskInProgress = false // флаг что таск на заливку ушел
    private var isGenerated = false
    private val algorithmsOnProcess = ArrayList<BaseAlgorithm>()

    // Собственно алгоритмы
    private enum class FillAlgorithm {
        MY_CUSTOM_ALGORITHM,
        DEEP_ALGORITHM,
        WIDTH_ALGORITHM
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        genButton = findViewById<Button>(R.id.gen_button)
        qrCode = findViewById<ImageView>(R.id.qrcode)
        qrCode2 = findViewById<ImageView>(R.id.qrcode2)
        speedBar = findViewById<SeekBar>(R.id.seekBar)
        sizeUp = findViewById<Button>(R.id.sizeplus)
        sizeDown = findViewById<Button>(R.id.sizeminus)
        viewGroupHolder = findViewById<LinearLayout>(R.id.view_holder);

        qrSetting1 = findViewById<Spinner>(R.id.qr_setting_1)
        qrSetting2 = findViewById<Spinner>(R.id.qr_setting_2)
        val adapter = ArrayAdapter.createFromResource(this, R.array.algorithms, android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        qrSetting1!!.adapter = adapter
        qrSetting2!!.adapter = adapter

        sizeUp!!.setOnClickListener { v ->
            if (pixelCountInSquare <= MAX_SQUARE_VALUE) {
                pixelCountInSquare *= 2
            }
        }
        sizeDown!!.setOnClickListener { v ->
            if (pixelCountInSquare >= MIN_SQUARE_VALUE) {
                pixelCountInSquare /= 2
            }
        }
        genButton!!.setOnClickListener { v -> generateQRCode() }
        qrCode!!.setOnTouchListener { v, event ->
            runFillViewTouch(event)
            false
        }
        qrCode2!!.setOnTouchListener { v, event ->
            runFillViewTouch(event)
            false
        }
    }

    private fun runFillViewTouch(event: MotionEvent) {
        if (fillTaskInProgress || !isGenerated) {
            return
        }
        fillTaskInProgress = true
        // row строка
        // column столбец
        val column = event.x.toInt() / pixelCountInSquare
        val row = event.y.toInt() / pixelCountInSquare
        algorithmsOnProcess.clear()

        val alg1 = qrSetting1!!.selectedItemPosition
        val alg2 = qrSetting2!!.selectedItemPosition

        algorithmsOnProcess.add(getAlgorithmByInt(alg1))
        algorithmsOnProcess.add(getAlgorithmByInt(alg2))
        val curColor = arrBoolean!![row][column]

        for (algorithmObject in algorithmsOnProcess) {
            algorithmObject.run(row, column, curColor)
        }

        val algResult1 = algorithmsOnProcess[0].resultOnUpdate
        val algResult2 = algorithmsOnProcess[1].resultOnUpdate

        subscrOnUpdate = Observable.interval((50 * (speedBar!!.max + 1 - speedBar!!.progress)).toLong(), TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap({
                    val onUpdate = algResult1[0]
                    algResult1.removeAt(0)
                    Observable.just(onUpdate)
                })
                .subscribe { onUpdateItem ->
                    for (i in onUpdateItem.first * pixelCountInSquare..onUpdateItem.first * pixelCountInSquare + pixelCountInSquare - 1) {
                        for (j in onUpdateItem.second * pixelCountInSquare..onUpdateItem.second * pixelCountInSquare + pixelCountInSquare - 1) {
                            currentBitmap!!.setPixel(j, i, if (curColor) Color.WHITE else Color.BLACK)
                        }
                    }
                    arrBoolean!![onUpdateItem.first][onUpdateItem.second] = !curColor
                    qrCode!!.setImageBitmap(currentBitmap)
                    if (algResult1.isEmpty()) {
                        subscrOnUpdate!!.unsubscribe()
                        if (subscrOnUpdate2!!.isUnsubscribed)
                            fillTaskInProgress = false
                    }
                }

        subscrOnUpdate2 = Observable.interval((50 * (speedBar!!.max + 1 - speedBar!!.progress)).toLong(), TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap({
                    val onUpdate = algResult2[0]
                    algResult2.removeAt(0)
                    Observable.just(onUpdate)
                })
                .subscribe { onUpdateItem ->
                    for (i in onUpdateItem.first * pixelCountInSquare..onUpdateItem.first * pixelCountInSquare + pixelCountInSquare - 1) {
                        for (j in onUpdateItem.second * pixelCountInSquare..onUpdateItem.second * pixelCountInSquare + pixelCountInSquare - 1) {
                            currentBitmap2!!.setPixel(j, i, if (curColor) Color.WHITE else Color.BLACK)
                        }
                    }
                    qrCode2!!.setImageBitmap(currentBitmap2)
                    if (algResult2.isEmpty()) {
                        subscrOnUpdate2!!.unsubscribe()
                        if (subscrOnUpdate!!.isUnsubscribed)
                            fillTaskInProgress = false
                    }
                }
    }

    private fun getAlgorithmByInt(alg: Int): BaseAlgorithm {
        when (alg) {
            0 -> return MyAlgorithm(arrBoolean)
            1 -> return MyAlgorithm2(arrBoolean)
            2 -> return MyAlgorithm3(arrBoolean)
            else -> return MyAlgorithm(arrBoolean)
        }
    }


    private fun generateQRCode() {
        if (fillTaskInProgress) {
            return
        }
        isGenerated = true;
        val width = qrCode!!.width - qrCode!!.width % 100
        val height = qrCode!!.height - qrCode!!.height % 100

        val qrcodeParams = qrCode!!.layoutParams as LinearLayout.LayoutParams
        qrcodeParams.width = width
        qrcodeParams.height = height
        qrCode!!.layoutParams = qrcodeParams
        qrCode2!!.layoutParams = qrcodeParams

        currentBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        currentBitmap2 = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        arrBoolean = Array(height / pixelCountInSquare) { BooleanArray(width / pixelCountInSquare) }
        val rand = Random()
        for (i in 0..height / pixelCountInSquare - 1) {
            for (j in 0..width / pixelCountInSquare - 1) {
                // default color : Black
                var colorToPut = Color.BLACK

                arrBoolean!![i][j] = true
                // Try to get a white pixel ;)
                if (rand.nextInt(2) == 0) {
                    colorToPut = Color.WHITE
                    arrBoolean!![i][j] = false
                }

                // Set color to (i,j) pixel
                for (k in i * pixelCountInSquare..i * pixelCountInSquare + pixelCountInSquare - 1) {
                    for (l in j * pixelCountInSquare..j * pixelCountInSquare + pixelCountInSquare - 1) {
                        try {
                            currentBitmap!!.setPixel(l, k, colorToPut)
                            currentBitmap2!!.setPixel(l, k, colorToPut)
                        } catch (e: Exception) {
                        }
                    }
                }
            }
        }

        qrCode!!.setImageBitmap(currentBitmap)
        qrCode2!!.setImageBitmap(currentBitmap2)
    }
}
