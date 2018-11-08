package ryley.wordsprint2;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mertakdut.BookSection;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ReaderView extends AppCompatActivity {

  BookParser bookparser = new BookParser(this);
  String[] currentSection = new String[0];

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

  ProgressBar progressBar;
  ImageView progressBackground;

  TextView wordTrack;
  TextView wordPreview1;
  TextView wordPreview2;
  TextView wordPreview3;
  TextView wordPreview4;
  TextView wordPreview5;
  TextView wordPreview6;
  TextView wordPreview7;
  TextView wordPreview8;

  boolean userIsWaiting = true;
  boolean isParsing = false;
  boolean isPlaying = false;
  int sectionPosition = 0;
  int wordPosition = 0;
  int wpmInMilli = 200;

  final int SEEK_BAR_PADDING = 50;
  final int ONE_WPM = 60000;
  final int DEFAULT_WPM = 300;

  String bookLoc = "";

  AlphaAnimation fadeOutHalfAnim = new AlphaAnimation(1.0f, 0.5f);
  AlphaAnimation fadeInHalfAnim = new AlphaAnimation(0.5f, 1.0f);

  AlphaAnimation fadeOutAnim = new AlphaAnimation(1.0f, 0.0f);
  AlphaAnimation fadeInAnim = new AlphaAnimation(0.0f, 1.0f);

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_reader_view);

    Intent intent = getIntent();
    bookLoc = intent.getStringExtra("BOOK");
    bookLoc = "PrideandPrejudice.epub";

    bindViews();

    if(parsedBook == null){
      parsedBook = new Book();
      parseBookAsync(bookLoc);
      setUpButtons();
      initAnimations();
      setUpSeekBar();
    }
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

  private void fadeInLoading(){
    wordTrack.setText(R.string.loading);
    progressBar.setVisibility(View.VISIBLE);
    progressBackground.setVisibility(View.VISIBLE);
    progressBar.animate().alpha(1).setDuration(100);
    progressBackground.animate().alpha(1).setDuration(100);
  }

  private void fadeOutLoading(){
    try {
      currentSection = parsedBook.getSection(sectionPosition);
    } catch (Exception e) {
      e.printStackTrace();
    }
    updateWords();
    progressBackground.animate().alpha(0).setDuration(600).withEndAction(() -> progressBackground.setVisibility(View.GONE));
    progressBar.animate().alpha(0).setDuration(600).withEndAction(() -> progressBar.setVisibility(View.GONE));
  }

  private void stopPlayer(){
    if(isPlaying){
      playerTimer.cancel();
      uiGroup.startAnimation(fadeInHalfAnim);
      changeButtonVisuals(true);
      wordTrack.bringToFront();
      updateWords();
      previewGroupTop.startAnimation(fadeInAnim);
      previewGroupBottom.startAnimation(fadeInAnim);
      isPlaying = false;
    }
  }

  private void startPlayer(){
    if(!isPlaying){
      setUpTimer(parsedBook, wordPosition, sectionPosition, wpmInMilli);
      changeButtonVisuals(false);
      playerTimer.start();
      uiGroup.startAnimation(fadeOutHalfAnim);
      wordTrack.bringToFront();
      previewGroupTop.startAnimation(fadeOutAnim);
      previewGroupBottom.startAnimation(fadeOutAnim);
      isPlaying = true;
    }
  }


  private void changeButtonVisuals(boolean isHighlighted){
    if(isHighlighted){
      playButton.setBackgroundResource(R.drawable.ic_play_arrow);
      playButton.setBackgroundTintList(getResources().getColorStateList(R.color.play_color, getTheme()));
      skipNextButton.setBackgroundTintList(getResources().getColorStateList(R.color.play_color, getTheme()));
      skipBackButton.setBackgroundTintList(getResources().getColorStateList(R.color.play_color, getTheme()));
      ff_Button.setBackgroundTintList(getResources().getColorStateList(R.color.play_color, getTheme()));
      rw_Button.setBackgroundTintList(getResources().getColorStateList(R.color.play_color, getTheme()));
    }
    else{
      playButton.setBackgroundResource(R.drawable.ic_pause_button);
      playButton.setBackgroundTintList(getResources().getColorStateList(R.color.pause_color, getTheme()));
      skipNextButton.setBackgroundTintList(getResources().getColorStateList(R.color.pause_color, getTheme()));
      skipBackButton.setBackgroundTintList(getResources().getColorStateList(R.color.pause_color, getTheme()));
      ff_Button.setBackgroundTintList(getResources().getColorStateList(R.color.pause_color, getTheme()));
      rw_Button.setBackgroundTintList(getResources().getColorStateList(R.color.pause_color, getTheme()));
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
      nextSection();
    });

    skipBackButton.setOnClickListener( v -> {
      if(wordPosition > 0){
        wordPosition = 0;
        updateWords();
      }
      else if(sectionPosition > 0){
        prevSection();
        updateWords();
      }
      else{
        Toast.makeText(this, "Beginning of Book", Toast.LENGTH_SHORT).show();
      }
    });

    ff_Button.setOnClickListener( v -> {
      if(currentSection.length >= (wordPosition + 5)) {
        wordPosition = wordPosition + 5;
        updateWords();
      }
      else{
        nextSection();
        Toast.makeText(this, "End of Section", Toast.LENGTH_SHORT).show();
      }
    });

    rw_Button.setOnClickListener( v -> {
      if(wordPosition >= 5) {
        wordPosition = wordPosition - 5;
        updateWords();
      }
      else if(wordPosition > 0){
        wordPosition = 0;
        updateWords();
      }
      else{
        Toast.makeText(this, "Beginning of Section", Toast.LENGTH_SHORT).show();
      }
    });


  }

  private void nextSection(){

    wordPosition = 0;

    //if reaching end of cached sections cache more of it
    if(sectionPosition >= (parsedBook.numOfSections() - 1) && !parsedBook.isCompleted()){
      if(sectionPosition == parsedBook.numOfSections()){
        userIsWaiting = true;
        fadeInLoading();
      }
      if(!isParsing){
        parseBookAsync(bookLoc);
      }
    }
    //only move to next section if it exists
    if(sectionPosition < parsedBook.numOfSections()){
      sectionPosition++;
    }
    else if (sectionPosition >= (parsedBook.numOfSections()) && parsedBook.isCompleted()){
      Toast.makeText(this, "End of Book", Toast.LENGTH_SHORT).show();
    }

    updateWords();
  }

  private void prevSection(){
    sectionPosition--;
  }

  private void updateWords(){
    try {
      currentSection = parsedBook.getSection(sectionPosition);

      if(wordPosition <= 0){
        wordTrack.setText(R.string.press_play_to_begin);
      }
      else{
        wordTrack.setText(currentSection[wordPosition]);
      }

      if(wordPosition > 3) fillInPreviewText(wordPreview1, wordPosition - 4);
      else wordPreview1.setText("");
      if(wordPosition > 2) fillInPreviewText(wordPreview2, wordPosition - 3);
      else wordPreview2.setText("");
      if(wordPosition > 1) fillInPreviewText(wordPreview3, wordPosition - 2);
      else wordPreview3.setText("");
      if(wordPosition > 0) fillInPreviewText(wordPreview4, wordPosition - 1);
      else wordPreview4.setText("");

      int remainingWords = currentSection.length - wordPosition;
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

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void fillInPreviewText(TextView preview, int position){
    preview.setText(currentSection[position]);
  }

  private void setUpTimer(Book parsedBook, final int wordPosition, final int linePosition, final int countDownInterval){

    //warm up buffer = 3 + 2 + 1
    final int WARM_UP_BUFFER = 6;
    long timerLength = 0;
    try {
      timerLength = parsedBook.getSection(linePosition).length + WARM_UP_BUFFER * countDownInterval;
    } catch (Exception e) {
      timerLength = 0;
      //TODO toast error
    }


    playerTimer = new CountDownTimer(timerLength, countDownInterval) {

      int warmUpTickSkip = 3;
      int warmUpTickGo = 3;
      int currentLine = linePosition;
      int currentWord = wordPosition;

      public void onTick(long millisUntilFinished) {

        try{
          currentSection = parsedBook.getSection(currentLine);

          //To make reading easier we're going to skip updating on some early ticks
          if(warmUpTickGo > 0){
            warmUpTickGo--;
          }
          else {
            if (warmUpTickSkip > 0) {
              warmUpTickSkip--;
              warmUpTickGo = warmUpTickSkip;
            }

            wordTrack.setText(currentSection[currentWord]);

            currentWord++;

            if (currentWord >= (currentSection.length)) {
              currentWord = 0;
              currentLine++;
            }

            if (currentLine >= parsedBook.numOfSections()) {
              onFinish();
            } else {
              setBookPosition(currentLine, currentWord);
            }
          }
        }
        catch (Exception e) {
          e.printStackTrace();
          playerTimer.cancel();
        }

    }

      public void onFinish() {
        nextSection();
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

    progressBar = findViewById(R.id.progress_Bar);
    progressBackground = findViewById(R.id.progress_background);

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

  private void parseBookAsync(String location){

    if(userIsWaiting){
      fadeInLoading();
    }

    Observable observableParser = Observable.create((ObservableOnSubscribe<BookSection>) e -> {
      //need a local copy to protect against async shenanigans
      int sectionPosition = this.sectionPosition;

      try{
        do{
          BookSection bookSection = bookparser.parseSection(location);
          e.onNext(bookSection);
        }
        while(bookparser.getCurrentSection() < (sectionPosition + 5));
      }
      catch (Exception error)
      {
        e.onError(error);
      }
      e.onComplete();
    });


    Observer observer = new Observer() {
      @Override
      public void onSubscribe(Disposable d) {
        isParsing = true;
      }

      @Override
      public void onNext(Object o) {
        parsedBook.addBookSection((BookSection) o);

        String message = "Section, ";
        if(((BookSection) o).getSectionTextContent() != null){
          message += ((BookSection) o).getSectionTextContent().substring(0,100);
        }
        Log.d("RDD", message);


        if(userIsWaiting) {
          userIsWaiting = false;
          fadeOutLoading();
        }
      }

      @Override
      public void onError(Throwable e) {
        Log.e("RDD ERROR","Yup, it failed, " + e.getMessage());
        parsedBook.setParsedCompleted(true);
      }

      @Override
      public void onComplete() {
        isParsing = false;
      }
    };

    observableParser
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.newThread())
            .subscribe(observer);

  }
}
