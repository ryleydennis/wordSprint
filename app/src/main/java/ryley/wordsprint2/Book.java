package ryley.wordsprint2;

import com.github.mertakdut.BookSection;

import java.util.ArrayList;
import java.util.List;

public class Book {
  private boolean parseCompleted = false;
  private List<String[]> parsedBook = new ArrayList<>();
  private long size = 0;

  public void addBookSection(BookSection bookSection){
    String[] parsedSection = bookSection.getSectionTextContent().split("\\s+");
    size += parsedSection.length;
    parsedBook.add(parsedSection);
  }

  public int numOfSections(){
    return parsedBook.size();
  }

  public String[] getSection(int section) throws Exception{
    try{
      return parsedBook.get(section);
    }
    catch (Exception e){
      throw e;
    }
  }

  public void setParsedCompleted(boolean completed){
    parseCompleted = completed;
  }

  public boolean isCompleted(){
    return parseCompleted;
  }




}
