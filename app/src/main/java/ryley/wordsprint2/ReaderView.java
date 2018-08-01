package ryley.wordsprint2;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.io.IOException;

public class ReaderView extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_reader_view);

    Intent intent = getIntent();
    String book = intent.getStringExtra("BOOK");
    TextView wordTrack = (TextView) findViewById(R.id.wordTrackView);


    BookParser parser = new BookParser(this);
    try {
      parser.parse("Doesn't matter right now");
    } catch (IOException e) {
      e.printStackTrace();
    }


    wordTrack.setText(book);

  }
}
