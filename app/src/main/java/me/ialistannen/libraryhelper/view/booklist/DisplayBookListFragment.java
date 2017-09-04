package me.ialistannen.libraryhelper.view.booklist;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindView;
import butterknife.ButterKnife;
import java.util.ArrayList;
import java.util.List;
import me.ialistannen.libraryhelper.R;
import me.ialistannen.libraryhelper.view.FragmentBase;
import me.ialistannen.libraryhelper.view.booklist.LoanableBookRecyclerList.ClickListener;
import me.ialistannen.libraryhelpercommon.book.LoanableBook;

/**
 * A fragment to display a list of books.
 */
public class DisplayBookListFragment extends FragmentBase {


  @BindView(R.id.book_list)
  LoanableBookRecyclerList recyclerView;

  private List<LoanableBook> books = new ArrayList<>();

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {

    View view = inflater.inflate(R.layout.fragment_display_book_list, container, false);

    ButterKnife.bind(this, view);

    recyclerView.setEmptyView(view.findViewById(R.id.empty_view));
    recyclerView.setBooks(books);
    recyclerView.setClickListener(new ClickListener() {
      @Override
      public void onClick(RecyclerView view, LoanableBook item, int index) {
        BookDetailFragment bookDetailFragment = new BookDetailFragment();
        bookDetailFragment.setBook(item);
        getFragmentHolderActivity().switchToFragmentPushBack(bookDetailFragment);
      }
    });

    return view;
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    getFragmentHolderActivity().setActionbarUpPopsFragment(true);
  }

  /**
   * @param books The {@link LoanableBook}s to add to this fragment
   */
  public void setBooks(List<LoanableBook> books) {
    this.books.addAll(books);
  }

}
