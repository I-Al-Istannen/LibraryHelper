package me.ialistannen.libraryhelper.view.booklist;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
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
import me.ialistannen.libraryhelper.util.PixelUtil;
import me.ialistannen.libraryhelpercommon.book.LoanableBook;

/**
 * A list with detailed information about a book.
 */
public class BookDetailList extends RecyclerView {

  {
    init();
  }

  private BookDataConverter dataConverter = new BookDataConverter();
  private ClickListener clickListener;
  private ContextMenuCreator contextMenuCreator;

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
    setNestedScrollingEnabled(false);

    addItemDecoration(new ItemDecoration() {
      private int spacingTop = PixelUtil.dpToPixels(getContext(), 18);
      private int spacingSides = PixelUtil.dpToPixels(getContext(), 8);

      @Override
      public void getItemOffsets(Rect outRect, View view, RecyclerView parent, State state) {
        super.getItemOffsets(outRect, view, parent, state);
        if (getChildAdapterPosition(view) != 0) {
          outRect.top = spacingTop;
        }
        outRect.bottom = spacingTop;
        outRect.left = spacingSides;
        outRect.right = spacingSides;
      }
    });
    DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
        getContext(), DividerItemDecoration.VERTICAL
    );
    dividerItemDecoration.setDrawable(
        ContextCompat.getDrawable(getContext(), R.drawable.highlighted_divider_horizontal)
    );
    addItemDecoration(dividerItemDecoration);
  }

  /**
   * @param book The {@link LoanableBook} to display
   */
  void setBook(LoanableBook book) {
    ((BookDetailAdapter) getAdapter()).setData(dataConverter.convert(book, getContext()));
  }

  /**
   * Sets the click listener.
   *
   * <p><strong>Must be called before the list adapter binds the views.</strong>
   *
   * @param clickListener The {@link ClickListener} to use
   */
  public void setClickListener(ClickListener clickListener) {
    this.clickListener = clickListener;
  }

  /**
   * Sets a listener to add context menus to list items.
   *
   * <p><strong>Must be called before the list adapter binds the views.</strong>
   *
   * @param contextMenuCreator The {@link ContextMenuCreator} to use
   */
  public void setContextMenuCreator(ContextMenuCreator contextMenuCreator) {
    this.contextMenuCreator = contextMenuCreator;
  }

  private class BookDetailAdapter extends Adapter {

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
      final BookDetailViewHolder detailViewHolder = (BookDetailViewHolder) holder;
      detailViewHolder.setData(data.get(position));

      if (clickListener != null) {
        detailViewHolder.getRootLayout().setOnClickListener(new OnClickListener() {
          @Override
          public void onClick(View v) {
            int childAdapterPosition = getChildAdapterPosition(detailViewHolder.getRootLayout());
            clickListener.onClick(
                BookDetailList.this,
                detailViewHolder.getRootLayout(),
                data.get(childAdapterPosition)
            );
          }
        });
      }
      if (contextMenuCreator != null) {
        detailViewHolder.getRootLayout().setOnCreateContextMenuListener(
            new OnCreateContextMenuListener() {
              @Override
              public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
                int childAdapterPosition = getChildAdapterPosition(
                    detailViewHolder.getRootLayout()
                );
                Pair<String, String> item = data.get(childAdapterPosition);
                contextMenuCreator.onCreateContextMenu(item, menu, v, menuInfo);
              }
            });
      }
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

    View getRootLayout() {
      return itemView.findViewById(R.id.display_book_detail_list_item_root);
    }

    void setData(Pair<String, String> data) {
      key.setText(data.getKey());
      value.setText(data.getValue());
    }
  }

  public interface ClickListener {

    /**
     * @param list The {@link BookDetailList} the click occurred in
     * @param view The clicked view
     * @param item The item that was clicked
     */
    void onClick(BookDetailList list, View view, Pair<String, String> item);
  }

  public interface ContextMenuCreator {

    /**
     * @param item The item that was clicked
     * @param menu The context menu
     * @param v The clicked view
     * @param menuInfo The {@link ContextMenuInfo}
     */
    void onCreateContextMenu(Pair<String, String> item, ContextMenu menu, View v,
        ContextMenuInfo menuInfo);
  }
}
