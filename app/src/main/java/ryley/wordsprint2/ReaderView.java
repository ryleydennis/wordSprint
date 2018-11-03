package ryley.wordsprint2;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;

public class ReaderView extends AppCompatActivity {

  TextView wordTrack;
  Button playButton;
  SeekBar wpmSlider;
  Book parsedBook;
  CountDownTimer playerTimer;
  TextView wpmTextView;
  boolean isPlaying = false;
  int linePosition = 0;
  int wordPosition = 0;
  int wpmInMilli = 200;

  final int SEEK_BAR_PADDING = 50;
  final int ONE_WPM = 60000;
  final int DEFAULT_WPM = 300;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_reader_view);

    Intent intent = getIntent();
    String bookLoc = intent.getStringExtra("BOOK");
    wordTrack = findViewById(R.id.wordTrackView);
    playButton = findViewById(R.id.play_button);
    wpmSlider = findViewById(R.id.seekBar);
    wpmTextView = findViewById(R.id.WPM_Textview);

    setUpPlayButton();
    setUpSeekBar();


    BookParser bookparser = new BookParser(this);

    try {
      parsedBook = new Book(bookparser.parse("Doesn't matter right now"));
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  private void setUpSeekBar(){
    wpmSlider.setMax(450);
    wpmSlider.setProgress(DEFAULT_WPM - 50);
    wpmSlider.setOnSeekBarChangeListener(
        new SeekBar.OnSeekBarChangeListener()
        {
          boolean temporaryPause = false;

          @Override
          public void onStopTrackingTouch(SeekBar seekBar) {
            if(temporaryPause){
              startPlayer();
              temporaryPause = false;
            }
          }

          @Override
          public void onStartTrackingTouch(SeekBar seekBar) {
            if(isPlaying){
              stopPlayer();
              temporaryPause = true;
            }
          }

          @Override
          public void onProgressChanged(SeekBar seekBar, int progress,
                                        boolean fromUser)
          {
            wpmTextView.setText(String.valueOf(progress + 50));
            wpmInMilli = ONE_WPM / (progress + SEEK_BAR_PADDING);
          }
        }
    );
  }

  private void stopPlayer(){
    if(isPlaying){
      playerTimer.cancel();
      playButton.setText("Play");
      isPlaying = false;
    }
  }

  private void startPlayer(){
    if(!isPlaying){
      setUpTimer(parsedBook, wordPosition, linePosition, wpmInMilli);
      playerTimer.start();
      playButton.setText("Pause");
      isPlaying = true;
    }
  }

  private void setUpPlayButton(){
    playButton.setOnClickListener( v -> {
      if(isPlaying){
        stopPlayer();
      }
      else{
        startPlayer();
      }
    });
  }

  private void setUpTimer(Book parsedBook, final int wordPosition, final int linePosition, final int countDownInterval){

    //warm up buffer = 5 + 4 + 3 + 2 + 1
    final int WARM_UP_BUFFER = 15;
    long timerLength = parsedBook.size() + WARM_UP_BUFFER * countDownInterval;


    playerTimer = new CountDownTimer(timerLength, countDownInterval) {

      int warmUpTickSkip = 5;
      int warmUpTickGo = 5;
      int currentLine = linePosition;
      int currentWord = wordPosition;
      List<String[]> parsedText = parsedBook.getParsedBook();

      public void onTick(long millisUntilFinished) {

        //To make reading easier we're going to skip updating on some early ticks
        if(warmUpTickGo > 0){
          warmUpTickGo--;
        }
        else{
          if(warmUpTickSkip > 0){
            warmUpTickSkip--;
            warmUpTickGo = warmUpTickSkip;
          }

          wordTrack.setText(parsedText.get(currentLine)[currentWord]);

          currentWord++;

          if(currentWord >= (parsedText.get(currentLine).length)) {
            currentWord = 0;
            currentLine++;
          }

          if(currentLine >= parsedText.size()){
            cancel();
          }

          else{
            setBookPosition(currentLine,currentWord);
          }
        }
      }

      public void onFinish() {

      }
    };
  }

  private void setBookPosition(int linePosition, int wordPosition){
    this.linePosition = linePosition;
    this.wordPosition = wordPosition;
  }
}
