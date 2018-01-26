/*
 *  Copyright 2017 Idealnaya rabota LLC
 *  Licensed under Multy.io license.
 *  See LICENSE for details
 */

package io.multy.ui.fragments;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import butterknife.OnClick;
import io.multy.R;
import io.multy.ui.adapters.BricksAdapter;
import io.multy.ui.fragments.dialogs.SimpleDialogFragment;
import io.multy.util.BrickView;
import io.multy.util.RandomSpanWidthLookup;


public class BaseSeedFragment extends BaseFragment {

    public static final int BRICK_BLUE = 0;
    public static final int BRICK_RED = 1;
    public static final int BRICK_GREEN = 2;

    private int redrawPosition = 0;
    private GridLayoutManager layoutManager;
    protected BricksAdapter adapter;

    protected void initBricks(RecyclerView recyclerView) {
        final int spanCount = 16 * 8; //total cell count * span per item
        layoutManager = new GridLayoutManager(getActivity(), spanCount);
        layoutManager.setSpanSizeLookup(new RandomSpanWidthLookup(spanCount));
        adapter = new BricksAdapter();

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
    }

    protected void setBrickColor(int color) {
        switch (color) {
            case BRICK_BLUE:
                adapter.setBrickBackgroundResId(R.drawable.brick_blue);
                break;
            case BRICK_RED:
                adapter.setBrickBackgroundResId(R.drawable.brick_red);
                break;
            case BRICK_GREEN:
                adapter.setBrickBackgroundResId(R.drawable.brick_green);
                break;
            default:
                adapter.setBrickBackgroundResId(-1);
        }
        adapter.notifyDataSetChanged();
    }

    protected void setRedrawPosition(int redrawPosition) {
        this.redrawPosition = redrawPosition;
    }

    /**
     * make three bricks (triplet) to pending (bordered) or full (fully colored)
     *
     * @param isPending pending or full
     */
    protected void redrawTriplet(boolean isPending) {
        BrickView brickView;
        for (int i = 0; i < 3; i++) {
            brickView = (BrickView) layoutManager.getChildAt(redrawPosition);

            if (isPending) {
                brickView.makePending();
            } else {
                brickView.makeFull();
            }
            redrawPosition++;
        }

        if (isPending) {
            redrawPosition -= 3;
        }
    }

    protected void redrawOne(boolean isPending) {
        BrickView brickView = (BrickView) layoutManager.getChildAt(redrawPosition);

        if (isPending) {
            brickView.makePending();
        } else {
            brickView.makeFull();
        }
        redrawPosition++;

        if (isPending) {
            redrawPosition -= 1;
        }
    }

    public void repeat() {
        getActivity().getSupportFragmentManager()
                .popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    public void showNext(Fragment fragment) {
        hideKeyboard(getActivity());
        showFragment(fragment, "");
    }

    public void showNext(Fragment fragment, String tag) {
        showFragment(fragment, tag);
    }

    private void showFragment(Fragment fragment, String tag) {
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container_main, fragment)
                .addToBackStack(tag)
                .commit();
    }

    public void close() {
        getActivity().finish();
    }

    @OnClick(R.id.button_cancel)
    public void onCancelClick() {
        SimpleDialogFragment.newInstance(
                R.string.cancel,
                R.string.cancel_message,
                view -> close()).show(getFragmentManager(), "");

//        new AlertDialog.Builder(getActivity())
//                .setTitle(R.string.cancel)
//                .setMessage(R.string.cancel_message)
//                .setPositiveButton(R.string.yes, (dialogInterface, i) -> close())
//                .setNegativeButton(R.string.no, (dialogInterface, i) -> dialogInterface.dismiss())
//                .show();
    }
}
