package com.hdweiss.morgand.gui.outline;

import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.hdweiss.morgand.Application;
import com.hdweiss.morgand.R;
import com.hdweiss.morgand.data.dao.OrgNode;
import com.hdweiss.morgand.data.dao.OrgNodeRepository;
import com.hdweiss.morgand.events.DataUpdatedEvent;
import com.hdweiss.morgand.gui.edit.BaseEditFragment;
import com.hdweiss.morgand.gui.edit.EditController;
import com.hdweiss.morgand.gui.edit.EditHeadingFragment;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

public class OutlineFragment extends Fragment {

    private final static String OUTLINE_NODES = "nodes";
    private final static String OUTLINE_LEVELS = "levels";
    private final static String OUTLINE_EXPANDED = "expanded";
    private final static String OUTLINE_CHECKED_POS = "selection";
    private final static String OUTLINE_SCROLL_POS = "scrollPosition";

    protected OutlineListView listView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Application.getBus().register(this);
        setHasOptionsMenu(true);
    }

    @Override
    public void onDestroy() {
        Application.getBus().unregister(this);
        super.onDestroy();
        OpenHelperManager.releaseHelper();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_outline, container, false);
        listView = (OutlineListView) rootView.findViewById(R.id.list);
        return rootView;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        listView.setActivity(getActivity());
        refreshView();
    }


    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        if (savedInstanceState == null)
            return;

        long[] state = savedInstanceState.getLongArray(OUTLINE_NODES);
        ArrayList<Integer> levels = savedInstanceState.getIntegerArrayList(OUTLINE_LEVELS);
        boolean[] expanded = savedInstanceState.getBooleanArray(OUTLINE_EXPANDED);
        if(state != null)
            listView.setState(state, levels, expanded);

        int checkedPos= savedInstanceState.getInt(OUTLINE_CHECKED_POS, 0);
        listView.setItemChecked(checkedPos, true);

        int scrollPos = savedInstanceState.getInt(OUTLINE_SCROLL_POS, 0);
        listView.setSelection(scrollPos);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLongArray(OUTLINE_NODES, listView.getNodeState());
        outState.putIntegerArrayList(OUTLINE_LEVELS, listView.getLevelState());
        outState.putBooleanArray(OUTLINE_EXPANDED, listView.getExpandedState());
        outState.putInt(OUTLINE_CHECKED_POS, listView.getCheckedItemPosition());
        outState.putInt(OUTLINE_SCROLL_POS, listView.getFirstVisiblePosition());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.outline, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int position = listView.getCheckedItemPosition();
        switch(item.getItemId()) {
            case android.R.id.home:
                if (listView != null)
                    listView.collapseCurrent();
                break;

            case R.id.add_child:
                showAddDialog(position);
                break;

            case R.id.edit:
                showEditDialog(position);
                break;

            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    private void showAddDialog(int position) {
        if (position < 0)
            return;

        OrgNode node = (OrgNode) listView.getAdapter().getItem(position);
        EditController editController = EditController.getAddNodeController(node, OrgNode.Type.Headline);
        BaseEditFragment fragment = new EditHeadingFragment(editController);
        showDialog(fragment);
    }

    private void showEditDialog(int position) {
        if (position < 0)
            return;

        OrgNode node = (OrgNode) listView.getAdapter().getItem(position);
        if (node == null)
            return;

        BaseEditFragment fragment = BaseEditFragment.getEditFragment(node);
        showDialog(fragment);
    }

    private void showDialog(DialogFragment fragment) {
        FragmentTransaction fragmentTransaction = getActivity().getFragmentManager().beginTransaction();
        fragmentTransaction.add(fragment, "dialog");
        fragmentTransaction.commit();
    }

    @Subscribe
    public void refreshView(DataUpdatedEvent event) {
        refreshView();
    }
    protected void refreshView() {
        listView.setData(OrgNodeRepository.getRootNodes());
    }
}
