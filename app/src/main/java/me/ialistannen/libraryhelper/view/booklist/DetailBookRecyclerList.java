package me.ialistannen.libraryhelper.view.booklist;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import me.ialistannen.isbnlookuplib.book.StandardBookDataKeys;
import me.ialistannen.isbnlookuplib.util.Pair;
import me.ialistannen.libraryhelper.R;
import me.ialistannen.libraryhelpercommon.book.LoanableBook;

/**
 * A {@link RecyclerView} to display {@link LoanableBook}s.
 */
public class DetailBookRecyclerList extends RecyclerView {

  // No real control over the constructors, so call for each
  {
    init();
  }

  public DetailBookRecyclerList(Context context) {
    super(context);
  }

  public DetailBookRecyclerList(Context context,
      @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  public DetailBookRecyclerList(Context context, @Nullable AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  private void init() {
    setLayoutManager(new LinearLayoutManager(getContext()));
    setAdapter(new BookAdapter());
    addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
    addItemDecoration(new ItemDecoration() {
      @Override
      public void getItemOffsets(Rect outRect, View view, RecyclerView parent, State state) {
        super.getItemOffsets(outRect, view, parent, state);
        if (parent.getChildAdapterPosition(view) != 0) {
          outRect.top = 20;
        }
        outRect.bottom = 20;
      }
    });
  }

  /**
   * @param books The books to add
   * @return This list
   */
  public DetailBookRecyclerList addBooks(LoanableBook... books) {
    return addBooks(Arrays.asList(books));
  }

  /**
   * @param books The {@link LoanableBook}s to add
   * @return This list
   */
  public DetailBookRecyclerList addBooks(Iterable<LoanableBook> books) {
    ((BookAdapter) getAdapter()).addBooks(books);
    return this;
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();
  }

  private static class BookAdapter extends Adapter {

    private List<LoanableBook> data = new ArrayList<>();

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
      return new BookViewHolder(
          layoutInflater.inflate(R.layout.fragment_display_book_list_item, parent, false)
      );
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
      ((BookViewHolder) holder).setBook(data.get(position));
    }

    @Override
    public int getItemCount() {
      return data.size();
    }

    /**
     * Adds books to the adapter.
     *
     * @param books The {@link LoanableBook}s to add
     */
    private void addBooks(Iterable<LoanableBook> books) {
      for (LoanableBook loanableBook : books) {
        data.add(loanableBook);
      }
      notifyDataSetChanged();
    }
  }

  static class BookViewHolder extends ViewHolder {

    @BindView(R.id.title_text_view)
    TextView title;

    @BindView(R.id.author_text_view)
    TextView author;

    private BookViewHolder(View itemView) {
      super(itemView);

      ButterKnife.bind(this, itemView);
    }

    private void setBook(LoanableBook book) {
      title.setText(book.getData(StandardBookDataKeys.TITLE).toString());
      List<Pair<String, String>> authors = book.getData(StandardBookDataKeys.AUTHORS);
      author.setText(authors.get(0).getKey());
    }
  }
}
