package me.ialistannen.libraryhelper.logic.query;

import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;
import java.util.List;
import me.ialistannen.libraryhelper.util.BasicFragmentServerCallback;
import me.ialistannen.libraryhelper.view.FragmentBase;
import me.ialistannen.libraryhelpercommon.book.IntermediaryBook;
import me.ialistannen.libraryhelpercommon.book.LoanableBook;

/**
 * A {@link BasicFragmentServerCallback} to extract books from the server.
 */
public abstract class BookExtractorServerCallback extends
    BasicFragmentServerCallback<List<IntermediaryBook>> {

  protected BookExtractorServerCallback(FragmentBase context, int dialogTitle) {
    super(context, dialogTitle, new TypeToken<List<IntermediaryBook>>() {
    }.getType());
  }

  @Override
  protected void onPojoReceived(List<IntermediaryBook> pojo) {
    List<LoanableBook> books = new ArrayList<>();
    for (IntermediaryBook intermediaryBook : pojo) {
      books.add(intermediaryBook.toLoanableBook());
    }

    onReceiveBooks(books);
  }

  /**
   * Called when the books are received from the server
   *
   * @param books The received books
   */
  protected abstract void onReceiveBooks(List<LoanableBook> books);
}
