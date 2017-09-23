package me.ialistannen.libraryhelper.view.booklist;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.google.common.base.Supplier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import me.ialistannen.isbnlookuplib.book.StandardBookDataKeys;
import me.ialistannen.isbnlookuplib.util.Pair;
import me.ialistannen.libraryhelper.R;
import me.ialistannen.libraryhelpercommon.book.LoanableBook;

/**
 * A {@link RecyclerView} to display {@link LoanableBook}s.
 */
public class LoanableBookRecyclerList extends RecyclerView {

  // No real control over the constructors, so call for each
  {
    init();
  }

  private View emptyView;
  private ClickListener clickListener;

  public LoanableBookRecyclerList(Context context) {
    super(context);
  }

  public LoanableBookRecyclerList(Context context,
      @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  public LoanableBookRecyclerList(Context context, @Nullable AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  private void init() {
    setLayoutManager(new LinearLayoutManager(getContext()));
    setAdapter(new BookAdapter());
    addItemDecoration(new ItemDecoration() {
      @Override
      public void getItemOffsets(Rect outRect, View view, RecyclerView parent, State state) {
        super.getItemOffsets(outRect, view, parent, state);
        if (parent.getChildAdapterPosition(view) != 0) {
          outRect.top = 25;
        }
        outRect.bottom = 25;
        outRect.right = 20;
      }
    });
  }

  /**
   * Sets the id of the view to be made visible when the list is empty.
   *
   * <p><br><strong>Must be invoked before calling {@link #setBooks(Collection)} to hide the view
   * correctly.</strong>
   *
   * @param emptyView The id of the view to display when the list is empty.
   */
  void setEmptyView(View emptyView) {
    this.emptyView = emptyView;
  }

  void setClickListener(ClickListener clickListener) {
    this.clickListener = clickListener;
  }

  /**
   * @param books The {@link LoanableBook}s to add
   */
  void setBooks(Collection<LoanableBook> books) {
    ((BookAdapter) getAdapter()).setBooks(books);
    if (emptyView == null) {
      return;
    }

    if (books.isEmpty()) {
      emptyView.setVisibility(VISIBLE);
      setVisibility(GONE);
    } else {
      emptyView.setVisibility(GONE);
      setVisibility(VISIBLE);
    }
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();
  }

  private class BookAdapter extends Adapter {

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
      final BookViewHolder bookViewHolder = (BookViewHolder) holder;
      bookViewHolder.setBook(data.get(position));

      if (clickListener == null) {
        return;
      }

      bookViewHolder.getRootLayout().setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          clickListener.onClick(
              LoanableBookRecyclerList.this,
              data.get(getChildAdapterPosition(bookViewHolder.getRootLayout())),
              getChildAdapterPosition(bookViewHolder.getRootLayout())
          );
        }
      });
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
    private void setBooks(Collection<LoanableBook> books) {
      data.clear();
      data.addAll(books);

      notifyDataSetChanged();
    }
  }

  static class BookViewHolder extends ViewHolder {

    @BindView(R.id.title_text_view)
    TextView title;

    @BindView(R.id.author_text_view)
    TextView author;

    @BindView(R.id.cover_image_view)
    ImageView coverImage;

    private BookViewHolder(View itemView) {
      super(itemView);

      ButterKnife.bind(this, itemView);
    }

    private View getRootLayout() {
      return itemView.findViewById(R.id.display_book_list_item_root);
    }

    private void setBook(final LoanableBook book) {
      title.setText(book.getData(StandardBookDataKeys.TITLE).toString());
      List<Pair<String, String>> authors = book.getData(StandardBookDataKeys.AUTHORS);
      author.setText(authors.get(0).getKey());

      coverImage.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
        @Override
        public boolean onPreDraw() {
          // only load it once
          coverImage.getViewTreeObserver().removeOnPreDrawListener(this);

          CoverUtil.withContext(new Supplier<Context>() {
            @Override
            public Context get() {
              return author.getContext();
            }
          })
              .withBook(book)
              .loadInto(coverImage);
          return true;
        }
      });
    }
  }

  public interface ClickListener {

    /**
     * @param view This view
     * @param item The item that was clicked
     * @param index The index it is in the adapter
     */
    void onClick(RecyclerView view, LoanableBook item, int index);
  }
}
