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
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import me.ialistannen.isbnlookuplib.util.Pair;
import me.ialistannen.libraryhelper.R;
import me.ialistannen.libraryhelpercommon.book.LoanableBook;

/**
 * A list with detailed information about a book.
 */
public class BookDetailList extends RecyclerView {

  {
    init();
  }

  private BookDataConverter dataConverter = new BookDataConverter();

  public BookDetailList(Context context) {
    super(context);
  }

  public BookDetailList(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  public BookDetailList(Context context, @Nullable AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  private void init() {
    setLayoutManager(new LinearLayoutManager(getContext()));
    setAdapter(new BookDetailAdapter());

    addItemDecoration(new ItemDecoration() {
      @Override
      public void getItemOffsets(Rect outRect, View view, RecyclerView parent, State state) {
        super.getItemOffsets(outRect, view, parent, state);
        if (getChildAdapterPosition(view) != 0) {
          outRect.top = 25;
        }
        outRect.bottom = 25;
      }
    });
  }

  /**
   * @param book The {@link LoanableBook} to display
   */
  void setBook(LoanableBook book) {
    ((BookDetailAdapter) getAdapter()).setData(dataConverter.convert(book, getContext()));
  }

  private static class BookDetailAdapter extends Adapter {

    private List<Pair<String, String>> data;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

      return new BookDetailViewHolder(
          layoutInflater.inflate(R.layout.fragment_display_book_detail_item, parent, false)
      );
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
      ((BookDetailViewHolder) holder).setData(data.get(position));
    }

    @Override
    public int getItemCount() {
      return data.size();
    }

    private void setData(List<Pair<String, String>> data) {
      this.data = data;
      Collections.sort(data, new Comparator<Pair<String, String>>() {
        @Override
        public int compare(Pair<String, String> o1, Pair<String, String> o2) {
          return o1.getKey().compareTo(o2.getKey());
        }
      });
      notifyDataSetChanged();
    }
  }

  static class BookDetailViewHolder extends ViewHolder {

    @BindView(R.id.text_view_key)
    TextView key;

    @BindView(R.id.text_view_value)
    TextView value;

    BookDetailViewHolder(View itemView) {
      super(itemView);

      ButterKnife.bind(this, itemView);
    }

    void setData(Pair<String, String> data) {
      key.setText(data.getKey());
      value.setText(data.getValue());
    }
  }
}
