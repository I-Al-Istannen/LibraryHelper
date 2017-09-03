package me.ialistannen.libraryhelper.view.booklist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindView;
import butterknife.ButterKnife;
import java.util.ArrayList;
import java.util.List;
import me.ialistannen.libraryhelper.R;
import me.ialistannen.libraryhelper.view.FragmentBase;
import me.ialistannen.libraryhelpercommon.book.LoanableBook;

/**
 * A fragment to display a list of books.
 */
public class DisplayBookListFragment extends FragmentBase {


  @BindView(R.id.book_list)
  DetailBookRecyclerList recyclerView;

  private List<LoanableBook> booksToAdd = new ArrayList<>();

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {

    View view = inflater.inflate(R.layout.fragment_display_book_list, container, false);

    ButterKnife.bind(this, view);

    recyclerView.addBooks(booksToAdd);
    booksToAdd.clear();

    return view;
  }

  /**
   * @param books The {@link LoanableBook}s to add to this fragment
   */
  public void addBooks(List<LoanableBook> books) {
    booksToAdd.addAll(books);
  }

}
