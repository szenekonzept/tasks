/**
 * Copyright (c) 2012 Todoroo Inc
 *
 * See the file "LICENSE" for the full license governing this code.
 */
package com.todoroo.astrid.core;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;

import com.todoroo.andlib.data.Callback;
import com.todoroo.andlib.sql.Criterion;
import com.todoroo.andlib.sql.Order;
import com.todoroo.andlib.sql.QueryTemplate;
import com.todoroo.andlib.utility.DialogUtilities;
import com.todoroo.astrid.api.AstridFilterExposer;
import com.todoroo.astrid.api.Filter;
import com.todoroo.astrid.api.FilterListItem;
import com.todoroo.astrid.dao.StoreObjectDao;
import com.todoroo.astrid.data.StoreObject;
import com.todoroo.astrid.data.Task;

import org.tasks.R;
import org.tasks.injection.ForApplication;
import org.tasks.injection.InjectingActivity;
import org.tasks.injection.InjectingBroadcastReceiver;
import org.tasks.injection.Injector;
import org.tasks.preferences.Preferences;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * Exposes Astrid's built in filters to the NavigationDrawerFragment
 *
 * @author Tim Su <tim@todoroo.com>
 *
 */
public final class CustomFilterExposer extends InjectingBroadcastReceiver implements AstridFilterExposer {

    private static final String TOKEN_FILTER_ID = "id"; //$NON-NLS-1$
    private static final String TOKEN_FILTER_NAME = "name"; //$NON-NLS-1$

    @Inject StoreObjectDao storeObjectDao;
    @Inject Preferences preferences;
    @Inject @ForApplication Context context;

    private FilterListItem[] prepareFilters() {
        Resources r = context.getResources();

        return buildSavedFilters(context, r);
    }

    private Filter[] buildSavedFilters(final Context context, Resources r) {
        final List<Filter> list = new ArrayList<>();

        // stock filters
        if (preferences.getBoolean(R.string.p_show_recently_modified_filter, true)) {
            Filter recent = new Filter(r.getString(R.string.BFE_Recent),
                    r.getString(R.string.BFE_Recent),
                    new QueryTemplate().where(
                            Criterion.all).orderBy(
                                    Order.desc(Task.MODIFICATION_DATE)).limit(15),
                                    null);
            list.add(recent);
        }

        storeObjectDao.getSavedFilters(new Callback<StoreObject>() {
            @Override
            public void apply(StoreObject savedFilter) {
                Filter f = SavedFilter.load(savedFilter);

                Intent deleteIntent = new Intent(context, DeleteActivity.class);
                deleteIntent.putExtra(TOKEN_FILTER_ID, savedFilter.getId());
                deleteIntent.putExtra(TOKEN_FILTER_NAME, f.title);
                f.contextMenuLabels = new String[] { context.getString(R.string.BFE_Saved_delete) };
                f.contextMenuIntents = new Intent[] { deleteIntent };
                list.add(f);

            }
        });

        return list.toArray(new Filter[list.size()]);
    }

    /**
     * Simple activity for deleting stuff
     *
     * @author Tim Su <tim@todoroo.com>
     *
     */
    public static class DeleteActivity extends InjectingActivity {

        @Inject StoreObjectDao storeObjectDao;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            setTheme(android.R.style.Theme_Dialog);
            super.onCreate(savedInstanceState);

            final long id = getIntent().getLongExtra(TOKEN_FILTER_ID, -1);
            if(id == -1) {
                finish();
                return;
            }
            final String name = getIntent().getStringExtra(TOKEN_FILTER_NAME);

            DialogUtilities.okCancelDialog(this,
                    getString(R.string.DLG_delete_this_item_question, name),
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            storeObjectDao.delete(id);
                            setResult(RESULT_OK);
                            finish();
                        }
                    },
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            setResult(RESULT_CANCELED);
                            finish();
                        }
                    });
        }
    }

    @Override
    public FilterListItem[] getFilters(Injector injector) {
        injector.inject(this);

        return prepareFilters();
    }

}
