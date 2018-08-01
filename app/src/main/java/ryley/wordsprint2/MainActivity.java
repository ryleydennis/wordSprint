package ryley.wordsprint2;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    Intent intent = new Intent(this, ReaderView.class);
    String currentBook = "Book Book Book";
    intent.putExtra("BOOK", currentBook);
    startActivity(intent);

  }
}
