package com.example.xieyangyi.myapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Message;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.xieyangyi.myapplication.board.GameConf;
import com.example.xieyangyi.myapplication.board.LinkInfo;
import com.example.xieyangyi.myapplication.service.GameService;
import com.example.xieyangyi.myapplication.view.GameView;
import com.example.xieyangyi.myapplication.view.Piece;

import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.LogRecord;
import android.os.Handler;


public class MainActivity extends Activity {
    private GameService gameService;
    private GameConf gameConf;
    private GameView gameView;
    private TextView txtLeftTime;
    private Button btnStart;
    private boolean isStarted = false;
    private Button btnPause;
    private boolean isPaused = false;
    private boolean isPlaying;
    private AlertDialog.Builder successDialog;
    private AlertDialog.Builder lostDialog;
    private Timer timer = new Timer();
    private int gameTime;
    private Piece selectedPiece;
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0x123:
                    txtLeftTime.setText("" + gameTime);
                    gameTime--;
                    if(gameTime < 0) {
                        stopTimer();
                        isPlaying = false;
                        lostDialog.show();
                        return;
                    }
                    break;
            }
        }
    };
    private Vibrator vibrator;
    private GameConf easyGameConf = new GameConf(8, 10, 100, 200, 100, this);
    private GameConf hardGameConf = new GameConf(10, 12, 50, 100, 70, this);
    private GameConf crazyGameConf = new GameConf(12, 14, 0, 50, 50, this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gameConf = easyGameConf;
        init();
    }

    public void init() {
        // get screen width and height
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screenWidth = dm.widthPixels;

        // game view display
        gameConf.setPieceWidth((screenWidth - 2 * gameConf.getxBeginPos()) / gameConf.getxSize());
        gameConf.setPieceHeight(gameConf.getPieceWidth());      // height and width are the same
        gameView = (GameView) findViewById(R.id.game_view);
        txtLeftTime = (TextView)findViewById(R.id.txt_left_time);
        gameService = new GameService(this.gameConf);
        gameView.setGameService(gameService);
        vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
        registerForContextMenu(gameView);       // context menu
        //gameView.startGame();

        // game view touch event
        gameView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!isPlaying || isPaused )
                    return false;
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    // find current piece
                    Piece[][] pieces = gameService.getPieces();
                    Piece currentPiece = gameService.findPiece(event.getX(), event.getY());
                    if (currentPiece == null)
                        return true;

                    // replace select piece if no piece selected before, otherwise link them
                    MainActivity.this.gameView.setSelectedPiece(currentPiece);
                    if (selectedPiece == null) {
                        selectedPiece = currentPiece;
                        MainActivity.this.gameView.postInvalidate();
                        return true;
                    } else {
                        LinkInfo linkInfo = gameService.link(selectedPiece, currentPiece);
                        if (linkInfo == null) {
                            selectedPiece = currentPiece;
                            MainActivity.this.gameView.postInvalidate();
                        } else {
                            handleSuccessLink(linkInfo, selectedPiece, currentPiece, pieces);
                        }
                    }
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    MainActivity.this.gameView.postInvalidate();
                }
                return true;
            }
        });

        // start button
        btnStart = (Button)findViewById(R.id.btn_start);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startGame(gameConf.getInitialLeftTime());
                isStarted = true;
            }
        });

        // pause button
        btnPause = (Button)findViewById(R.id.btn_pause);
        btnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isStarted) {
                    if (isPaused) {
                        startGame(gameTime);
                        isPaused = false;
                        btnPause.setText("pause");
                    } else {
                        stopTimer();
                        isPaused = true;
                        btnPause.setText("continue");
                    }
                }
            }
        });

        // success & lost dialog
        successDialog = new AlertDialog.Builder(this)
                .setTitle("success")
                .setMessage("game success, play again!")
                .setIcon(R.drawable.success)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startGame(gameConf.getInitialLeftTime());
                    }
                });
        lostDialog = new AlertDialog.Builder(this)
                .setTitle("lost")
                .setMessage("game lost, play again!")
                .setIcon(R.drawable.lost)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startGame(gameConf.getInitialLeftTime());
                    }
                });


    }
    private void startGame(int gameTime) {
        if(this.timer != null)
            stopTimer();

        // if game time equals to the total game time, start game again
        this.gameTime = gameTime;
        if(gameTime == gameConf.getInitialLeftTime()) {
            this.gameView.startGame();
        }
        isPlaying = true;

        // start new timer task, this is mainly for setting the left time
        this.timer = new Timer();
        this.timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.sendEmptyMessage(0x123);
            }
        }, 0, 1000);        // delay 0s, every 1s start the task

        this.selectedPiece = null;
    }

    private void handleSuccessLink(LinkInfo linkInfo, Piece prePiece, Piece currentPiece, Piece[][] pieces) {
        // draw link lines in game view
        this.gameView.setLinkInfo(linkInfo);
        this.gameView.setSelectedPiece(null);
        this.gameView.postInvalidate();
        this.vibrator.vibrate(100);

        // set the 2 pieces as null
        pieces[prePiece.getxIndex()][prePiece.getyIndex()] = null;
        pieces[currentPiece.getxIndex()][currentPiece.getyIndex()] = null;
        this.selectedPiece = null;

        // if no piece left, game success
        if(!gameService.hasPieces()) {
            isPlaying = false;
            stopTimer();
            successDialog.show();
        }
    }

    private void stopTimer() {
        this.timer.cancel();
        this.timer = null;
    }

    protected void onPause() {
        stopTimer();
        super.onPause();
    }

    protected void onResume() {
        if(isPlaying)
            startGame(gameTime);
        super.onResume();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getMenuInflater().inflate(R.menu.menu_context, menu);
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
                    case R.id.difficulty_easy:
                        this.gameConf = easyGameConf;
                        init();
                        break;
                    case R.id.difficulty_hard:
                        this.gameConf = hardGameConf;
                        init();
                        break;
                    case R.id.difficulty_crazy:
                        this.gameConf = crazyGameConf;
                        init();
                        break;
                    default:
                        break;
        }
        return super.onContextItemSelected(item);
    }
}
