package ryley.wordsprint2;

import java.util.ArrayList;
import java.util.List;

public class Book {
  private List<String[]> parsedBook = new ArrayList<>();
  private long size = 0;

  Book(List<String[]> parsedBook){
    this.parsedBook = parsedBook;
    setBookSize();
  }

  public void setParsedBook(List<String[]> parsedBook) {
    this.parsedBook = parsedBook;
    setBookSize();
  }

  public List<String[]> getParsedBook(){
    return parsedBook;
  }

  public long size(){
    return size;
  }

  private void setBookSize(){
    size = 0;

    for(int i = 0; i < parsedBook.size(); i++){
      for(int j = 0; j < parsedBook.get(i).length; j++){
        size++;
      }
    }
  }

}
