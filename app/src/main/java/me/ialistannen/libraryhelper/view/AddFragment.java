package me.ialistannen.libraryhelper.view;

import android.content.Context;
import me.ialistannen.isbnlookuplib.isbn.Isbn;

/**
 * A fragment that allows for adding new books.
 */
public class AddFragment extends IsbnInputFragment {

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    getFragmentHolderActivity().setActionbarUpPopsFragment(true);
  }

  @Override
  protected void consumeIsbn(Isbn isbn) {
    FragmentBookAddPreview fragmentBookAddPreview = new FragmentBookAddPreview();
    fragmentBookAddPreview.setIsbn(isbn);
    getFragmentHolderActivity().switchToFragmentPushBack(fragmentBookAddPreview);
  }

}
