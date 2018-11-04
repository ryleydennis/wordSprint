package ryley.wordsprint2;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mertakdut.exception.OutOfPagesException;
import com.github.mertakdut.exception.ReadingException;

import java.io.IOException;
import java.util.List;

public class ReaderView extends AppCompatActivity {


  Button skipNextButton;
  Button skipBackButton;
  Button ff_Button;
  Button rw_Button;
  Button playButton;
  TextView wpmTextView;
  SeekBar wpmSlider;
  Book parsedBook;
  CountDownTimer playerTimer;
  ConstraintLayout uiGroup;
  ConstraintLayout previewGroupTop;
  ConstraintLayout previewGroupBottom;

  TextView wordTrack;
  TextView wordPreview1;
  TextView wordPreview2;
  TextView wordPreview3;
  TextView wordPreview4;
  TextView wordPreview5;
  TextView wordPreview6;
  TextView wordPreview7;
  TextView wordPreview8;

  boolean isPlaying = false;
  int sectionPosition = 0;
  int wordPosition = 0;
  int wpmInMilli = 200;

  final int SEEK_BAR_PADDING = 50;
  final int ONE_WPM = 60000;
  final int DEFAULT_WPM = 300;

  AlphaAnimation fadeOutHalfAnim = new AlphaAnimation(1.0f, 0.5f);
  AlphaAnimation fadeInHalfAnim = new AlphaAnimation(0.5f, 1.0f);

  AlphaAnimation fadeOutAnim = new AlphaAnimation(1.0f, 0.0f);
  AlphaAnimation fadeInAnim = new AlphaAnimation(0.0f, 1.0f);

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_reader_view);

    Intent intent = getIntent();
    String bookLoc = intent.getStringExtra("BOOK");



    BookParser bookparser = new BookParser(this);

    try {
      parsedBook = new Book(bookparser.parse("PrideandPrejudice.epub"));
    } catch (IOException | ReadingException | OutOfPagesException e) {
      e.printStackTrace();
    }

    //TODO find better way to remove intro chapters
    parsedBook.getParsedBook().remove(0);
    parsedBook.getParsedBook().remove(0);

    bindViews();
    setUpButtons();
    setUpSeekBar();
    initAnimations();
    updatePreviewGroups();
  }

  private void setUpSeekBar(){
    wpmSlider.getProgressDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
    wpmSlider.getThumb().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
    wpmSlider.setThumb(getDrawable(R.drawable.ic_adjust_black_24dp));

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
      playButton.setBackgroundResource(R.drawable.ic_play_arrow);
      playButton.setBackgroundTintList(getResources().getColorStateList(R.color.play_color, getTheme()));
      uiGroup.startAnimation(fadeInHalfAnim);
      wordTrack.bringToFront();
      updatePreviewGroups();
      previewGroupTop.startAnimation(fadeInAnim);
      previewGroupBottom.startAnimation(fadeInAnim);

      isPlaying = false;
    }
  }

  private void startPlayer(){
    if(!isPlaying){
      setUpTimer(parsedBook, wordPosition, sectionPosition, wpmInMilli);
      playerTimer.start();
      wordTrack.bringToFront();
      playButton.setBackgroundResource(R.drawable.ic_pause_button);
      playButton.setBackgroundTintList(getResources().getColorStateList(R.color.pause_color, getTheme()));
      uiGroup.startAnimation(fadeOutHalfAnim);
      previewGroupTop.startAnimation(fadeOutAnim);
      previewGroupBottom.startAnimation(fadeOutAnim);
      isPlaying = true;
    }
  }


  private void setUpButtons(){
    playButton.setOnClickListener( v -> {
      if(isPlaying){
        stopPlayer();
      }
      else{
        startPlayer();
      }
    });

    skipNextButton.setOnClickListener( v -> {
      if(parsedBook.getParsedBook().size() > sectionPosition) {
        sectionPosition++;
        wordPosition = 0;
        updatePreviewGroups();
        updateWordTrack();
      }
      else{
        Toast.makeText(this, "End of Book", Toast.LENGTH_SHORT).show();
      }
    });

    skipBackButton.setOnClickListener( v -> {
      if(sectionPosition > 0) {

        if(wordPosition != 0){
          wordPosition = 0;
        }
        else{
          sectionPosition--;
        }
        updatePreviewGroups();
        updateWordTrack();
      }
      else{
        Toast.makeText(this, "Beginning of Book", Toast.LENGTH_SHORT).show();
      }
    });

    ff_Button.setOnClickListener( v -> {
      if(parsedBook.getParsedBook().get(sectionPosition).length >= (wordPosition + 5)) {
        wordPosition = wordPosition + 5;
        updatePreviewGroups();
        updateWordTrack();
      }
      else{
        Toast.makeText(this, "End of Section", Toast.LENGTH_SHORT).show();
      }
    });

    rw_Button.setOnClickListener( v -> {
      if(wordPosition >= 5) {
        wordPosition = wordPosition - 5;
        updatePreviewGroups();
        updateWordTrack();
      }
      else{
        Toast.makeText(this, "Beginning of Section", Toast.LENGTH_SHORT).show();
      }
    });


  }

  private void updateWordTrack(){
    if(wordPosition == 0){
      wordTrack.setText("Press Play To Begin...");
    }
    else{
    wordTrack.setText(parsedBook.getParsedBook().get(sectionPosition)[wordPosition]);
    }
  }

  private void updatePreviewGroups(){

    if(wordPosition > 3) fillInPreviewText(wordPreview1, wordPosition - 4);
    else wordPreview1.setText("");
    if(wordPosition > 2) fillInPreviewText(wordPreview2, wordPosition - 3);
    else wordPreview2.setText("");
    if(wordPosition > 1) fillInPreviewText(wordPreview3, wordPosition - 2);
    else wordPreview3.setText("");
    if(wordPosition > 0) fillInPreviewText(wordPreview4, wordPosition - 1);
    else wordPreview4.setText("");

    int remainingWords = parsedBook.getParsedBook().get(sectionPosition).length - wordPosition;
    //If haven't started, show first word in preview instead of second
    int startBuffer = (wordPosition == 0 ? -1 : 0);

    if(remainingWords > 0) fillInPreviewText(wordPreview5, wordPosition + 1 + startBuffer);
    else wordPreview5.setText("");
    if(remainingWords > 1) fillInPreviewText(wordPreview6, wordPosition + 2 + startBuffer);
    else wordPreview6.setText("");
    if(remainingWords > 2) fillInPreviewText(wordPreview7, wordPosition + 3 + startBuffer);
    else wordPreview7.setText("");
    if(remainingWords > 3) fillInPreviewText(wordPreview8, wordPosition + 4 + startBuffer);
    else wordPreview8.setText("");
  }


  private void fillInPreviewText(TextView preview, int position){
    preview.setText(parsedBook.getParsedBook().get(sectionPosition)[position]);
  }

  private void setUpTimer(Book parsedBook, final int wordPosition, final int linePosition, final int countDownInterval){

    //warm up buffer = 3 + 2 + 1
    final int WARM_UP_BUFFER = 6;
    long timerLength = parsedBook.size() + WARM_UP_BUFFER * countDownInterval;


    playerTimer = new CountDownTimer(timerLength, countDownInterval) {

      int warmUpTickSkip = 3;
      int warmUpTickGo = 3;
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
    this.sectionPosition = linePosition;
    this.wordPosition = wordPosition;
  }

  private void bindViews(){
    skipNextButton = findViewById(R.id.skipNext_button);
    skipBackButton = findViewById(R.id.skipBack_button);
    rw_Button = findViewById(R.id.rw_button);
    ff_Button = findViewById(R.id.ff_button);
    wordTrack = findViewById(R.id.wordTrackView);
    playButton = findViewById(R.id.play_button);
    wpmSlider = findViewById(R.id.seekBar);
    wpmTextView = findViewById(R.id.WPM_Textview);
    uiGroup = findViewById(R.id.ui_group);
    previewGroupTop = findViewById(R.id.previewGroupTop);
    previewGroupBottom = findViewById(R.id.previewGroupBottom);

    wordPreview1 = findViewById(R.id.word_preview_1);
    wordPreview2 = findViewById(R.id.word_preview_2);
    wordPreview3 = findViewById(R.id.word_preview_3);
    wordPreview4 = findViewById(R.id.word_preview_4);
    wordPreview5 = findViewById(R.id.word_preview_5);
    wordPreview6 = findViewById(R.id.word_preview_6);
    wordPreview7 = findViewById(R.id.word_preview_7);
    wordPreview8 = findViewById(R.id.word_preview_8);
  }

  private void initAnimations(){
    fadeOutHalfAnim.setDuration(500);
    fadeOutHalfAnim.setFillAfter(true);
    fadeOutHalfAnim.setFillEnabled(true);
    fadeInHalfAnim.setDuration(500);
    fadeInHalfAnim.setFillAfter(true);
    fadeInHalfAnim.setFillEnabled(true);
    fadeOutAnim.setDuration(500);
    fadeOutAnim.setFillAfter(true);
    fadeOutAnim.setFillEnabled(true);
    fadeInAnim.setDuration(500);
    fadeInAnim.setFillAfter(true);
    fadeInAnim.setFillEnabled(true);
  }
}
