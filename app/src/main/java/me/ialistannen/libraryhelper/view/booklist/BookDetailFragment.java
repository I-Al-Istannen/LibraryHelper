package me.ialistannen.libraryhelper.view.booklist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.squareup.picasso.Picasso;
import me.ialistannen.isbnlookuplib.book.StandardBookDataKeys;
import me.ialistannen.isbnlookuplib.isbn.Isbn;
import me.ialistannen.libraryhelper.R;
import me.ialistannen.libraryhelper.util.HttpUtil;
import me.ialistannen.libraryhelper.util.HttpUtil.EndpointType;
import me.ialistannen.libraryhelper.view.FragmentBase;
import me.ialistannen.libraryhelpercommon.book.LoanableBook;

/**
 * A fragment to display detail information about a book.
 */
public class BookDetailFragment extends FragmentBase {

  @BindView(R.id.book_detail_list)
  BookDetailList detailList;

  private boolean coverAdded;
  private LoanableBook book;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {

    coverAdded = false;

    View view = inflater.inflate(R.layout.fragment_book_detail, container, false);

    ButterKnife.bind(this, view);

    if (book != null) {
      setCover(view);
      detailList.setBook(book);

      String title = book.getData(StandardBookDataKeys.TITLE);
      ((TextView) view.findViewById(R.id.book_title_text_view)).setText(title);
    }

    return view;
  }

  private void setCover(View view) {
    final ImageView cover = view.findViewById(R.id.cover_image_view);
    cover.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
      @Override
      public boolean onPreDraw() {
        if (coverAdded) {
          return true;
        }

        Picasso.with(getFragmentHolderActivity())
            .load(buildCoverImageUrl(book))
            .resize(cover.getWidth(), cover.getHeight())
            .centerInside()
            .into(cover);
        coverAdded = true;
        return true;
      }
    });
  }

  public void setBook(LoanableBook book) {
    this.book = book;
  }

  private String buildCoverImageUrl(LoanableBook book) {
    Isbn isbn = book.getData(StandardBookDataKeys.ISBN);
    return HttpUtil.getServerUrlFromSettings(getFragmentHolderActivity(), EndpointType.COVER)
        .url()
        .toExternalForm()
        + "/" + isbn.getDigitsAsString();
  }
}
