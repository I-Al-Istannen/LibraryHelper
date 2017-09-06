package me.ialistannen.libraryhelper.view.booklist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.ButterKnife;
import me.ialistannen.libraryhelper.R;
import me.ialistannen.libraryhelper.view.FragmentBase;
import me.ialistannen.libraryhelpercommon.book.LoanableBook;

/**
 * A fragment to display detail information about a book.
 */
public class BookDetailFragment extends FragmentBase {

  private LoanableBook book;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {

    View view = inflater.inflate(R.layout.fragment_book_detail, container, false);

    ButterKnife.bind(this, view);

    if (book != null) {
      BookDetailLayout detailLayout = view.findViewById(R.id.book_detail_layout);
      detailLayout.setBook(book);
    }

    return view;
  }

  public void setBook(LoanableBook book) {
    this.book = book;
  }
}
