package me.ialistannen.libraryhelper.view.booklist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import me.ialistannen.libraryhelper.R;
import me.ialistannen.libraryhelper.view.FragmentBase;

/**
 * A fragment to display detail information about a book.
 */
public class BookDetailFragment extends FragmentBase {

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {

    View view = inflater.inflate(R.layout.fragment_book_detail, container, false);

    return view;
  }
}
