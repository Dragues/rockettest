package com.example.ilya.myrocketapp;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Pair;
import android.view.MotionEvent;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private Button genButton;
    private Button sizeUp;
    private Button sizeDown;
    private SeekBar speedBar;
    private ImageView qrCode;
    private ImageView qrCode2;
    private Spinner qrSetting1;
    private Spinner qrSetting2;
    private Bitmap currentBitmap;
    private Bitmap currentBitmap2;

    private int pixelCountInSquare = 25;
    private int MAX_SQUARE_VALUE = 200;
    private int MIN_SQUARE_VALUE = 25;

    // MY ALGORITHM
    private boolean arrBoolean[][];// состояния пикселей битмапки (чтобы не вычитывать пожертвуем памятью) Массив boolean можем себе позволить
    private boolean checkedArray[][];
    private boolean checkedArray2[][];// элемент работы алгоритма (показывает были ли мы в этой ячейке или нет при рекурсии)
    private ArrayList<Pair<Integer, Integer>> onRevertArray = new ArrayList<>(); // квадраты на перекраску после завершнения работы алгоритма
    private ArrayList<Pair<Integer, Integer>> onRevertArray2 = new ArrayList<>(); // квадраты на перекраску после завершнения работы алгоритма

    private Subscription subscrOnUpdate; // подписка на апдейт состояния view
    private Subscription subscrOnUpdate2; // подписка на апдейт состояния view
    private boolean fillTaskInProgress = false; // флаг что таск на заливку ушел
    private ArrayList<BaseAlgorithm> algorithmsOnProcess = new ArrayList<>();;

    // Собственно алгоритмы
    private enum FillAlgorithm {
        MY_CUSTOM_ALGORITHM,
        DEEP_ALGORITHM,
        WIDTH_ALGORITHM
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        genButton = (Button) findViewById(R.id.gen_button);
        qrCode = (ImageView) findViewById(R.id.qrcode);
        qrCode2 = (ImageView) findViewById(R.id.qrcode2);
        speedBar = (SeekBar) findViewById(R.id.seekBar);
        sizeUp = (Button) findViewById(R.id.sizeplus);
        sizeDown = (Button) findViewById(R.id.sizeminus);

        qrSetting1 = (Spinner) findViewById(R.id.qr_setting_1);
        qrSetting2 = (Spinner) findViewById(R.id.qr_setting_2);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.algorithms, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        qrSetting1.setAdapter(adapter);
        qrSetting2.setAdapter(adapter);

        sizeUp.setOnClickListener(v -> {
            if (pixelCountInSquare <= MAX_SQUARE_VALUE) {
                pixelCountInSquare *= 2;
            }
        });
        sizeDown.setOnClickListener(v -> {
            if (pixelCountInSquare >= MIN_SQUARE_VALUE) {
                pixelCountInSquare /= 2;
            }
        });
        genButton.setOnClickListener(v -> generateQRCode());
        qrCode.setOnTouchListener((v, event) -> {
            runFillViewTouch(event);
            return false;
        });
        qrCode2.setOnTouchListener((v, event) -> {
            runFillViewTouch(event);
            return false;
        });
    }

    private void runFillViewTouch(MotionEvent event) {
        if (fillTaskInProgress) {
            return;
        }
        fillTaskInProgress = true;
        // row строка
        // column столбец
        int column = (int) event.getX() / pixelCountInSquare;
        int row = (int) event.getY() / pixelCountInSquare;
        algorithmsOnProcess.clear();

        int alg1 = qrSetting1.getSelectedItemPosition();
        int alg2 = qrSetting2.getSelectedItemPosition();

        algorithmsOnProcess.add(getAlgorithmByInt(alg1));
        algorithmsOnProcess.add(getAlgorithmByInt(alg2));
        boolean curColor = arrBoolean[row][column];

        for (BaseAlgorithm algorithmObject : algorithmsOnProcess) {
            algorithmObject.run(row, column, curColor);
        }

        ArrayList<Pair<Integer, Integer>> algResult1 = algorithmsOnProcess.get(0).getResultOnUpdate();
        ArrayList<Pair<Integer, Integer>> algResult2 = algorithmsOnProcess.get(1).getResultOnUpdate();

        subscrOnUpdate = Observable.interval(50 * (speedBar.getMax() + 1 - speedBar.getProgress()), TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap((Func1<Long, Observable<Pair<Integer, Integer>>>) ignore -> {
                    Pair<Integer, Integer> onUpdate = algResult1.get(0);
                    algResult1.remove(0);
                    return Observable.just(onUpdate);
                })
                .subscribe(onUpdateItem -> {
                    for (int i = onUpdateItem.first * pixelCountInSquare; i < onUpdateItem.first * pixelCountInSquare + pixelCountInSquare; i++) {
                        for (int j = onUpdateItem.second * pixelCountInSquare; j < onUpdateItem.second * pixelCountInSquare + pixelCountInSquare; j++) {
                            currentBitmap.setPixel(j, i, curColor ? Color.WHITE : Color.BLACK);
                        }
                    }
                    arrBoolean[onUpdateItem.first][onUpdateItem.second] = !curColor;
                    qrCode.setImageBitmap(currentBitmap);
                    if (algResult1.isEmpty()) {
                        subscrOnUpdate.unsubscribe();
                        fillTaskInProgress = false;
                    }
                });

        subscrOnUpdate2 = Observable.interval(50 * (speedBar.getMax() + 1 - speedBar.getProgress()), TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap((Func1<Long, Observable<Pair<Integer, Integer>>>) ignore -> {
                    Pair<Integer, Integer> onUpdate = algResult2.get(0);
                    algResult2.remove(0);
                    return Observable.just(onUpdate);
                })
                .subscribe(onUpdateItem -> {
                    for (int i = onUpdateItem.first * pixelCountInSquare; i < onUpdateItem.first * pixelCountInSquare + pixelCountInSquare; i++) {
                        for (int j = onUpdateItem.second * pixelCountInSquare; j < onUpdateItem.second * pixelCountInSquare + pixelCountInSquare; j++) {
                            currentBitmap2.setPixel(j, i, curColor ? Color.WHITE : Color.BLACK);
                        }
                    }
                    qrCode2.setImageBitmap(currentBitmap2);
                    if (algResult2.isEmpty()) {
                        subscrOnUpdate2.unsubscribe();
                        fillTaskInProgress = false;
                    }
                });
    }

    private BaseAlgorithm getAlgorithmByInt(int alg) {
        switch (alg) {
            case 0:
                return new MyAlgorithm(arrBoolean);
            case 1:
                return new MyAlgorithm2(arrBoolean);
            case 2:
                return new MyAlgorithm3(arrBoolean);
            default:
                return new MyAlgorithm(arrBoolean);
        }
    }

    private void clearCheckedArrays() {
        for (int i = 0; i < checkedArray.length; i++) {
            for (int j = 0; j < checkedArray[i].length; j++) {
                checkedArray[i][j] = false;
                checkedArray2[i][j] = false;
            }
        }
    }


    private void generateQRCode() {
        if (fillTaskInProgress) {
            return;
        }
        int width = qrCode.getWidth() - qrCode.getWidth() % 100;
        int height = qrCode.getHeight() - qrCode.getHeight() % 100;

        LinearLayout.LayoutParams qrcodeParams = (LinearLayout.LayoutParams) qrCode.getLayoutParams();
        qrcodeParams.width = width;
        qrcodeParams.height = height;
        qrCode.setLayoutParams(qrcodeParams);

        currentBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        currentBitmap2 = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        arrBoolean = new boolean[height / pixelCountInSquare][width / pixelCountInSquare];
        Random rand = new Random();
        for (int i = 0; i < height / pixelCountInSquare; i++) {
            for (int j = 0; j < width / pixelCountInSquare; j++) {
                // default color : Black
                int colorToPut = Color.BLACK;

                arrBoolean[i][j] = true;
                // Try to get a white pixel ;)
                if (rand.nextInt(2) == 0) {
                    colorToPut = Color.WHITE;
                    arrBoolean[i][j] = false;
                }

                // Set color to (i,j) pixel
                for (int k = i * pixelCountInSquare; k < i * pixelCountInSquare + pixelCountInSquare; k++) {
                    for (int l = j * pixelCountInSquare; l < j * pixelCountInSquare + pixelCountInSquare; l++) {
                        currentBitmap.setPixel(l, k, colorToPut);
                        currentBitmap2.setPixel(l, k, colorToPut);
                    }
                }
            }
        }

        qrCode.setImageBitmap(currentBitmap);
        qrCode2.setImageBitmap(currentBitmap2);
    }
}
