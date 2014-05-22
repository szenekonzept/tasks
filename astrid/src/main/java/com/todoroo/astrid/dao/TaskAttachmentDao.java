/**
 * Copyright (c) 2012 Todoroo Inc
 *
 * See the file "LICENSE" for the full license governing this code.
 */
package com.todoroo.astrid.dao;

import com.todoroo.andlib.data.TodorooCursor;
import com.todoroo.andlib.sql.Criterion;
import com.todoroo.andlib.sql.Query;
import com.todoroo.astrid.data.TagData;
import com.todoroo.astrid.data.TaskAttachment;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Data Access layer for {@link TagData}-related operations.
 *
 * @author Tim Su <tim@todoroo.com>
 *
 */
@Singleton
public class TaskAttachmentDao extends RemoteModelDao<TaskAttachment> {

    @Inject
	public TaskAttachmentDao(Database database) {
        super(TaskAttachment.class);
        setDatabase(database);
    }

    public boolean taskHasAttachments(String taskUuid) {
        TodorooCursor<TaskAttachment> files = query(Query.select(TaskAttachment.TASK_UUID).where(
                        Criterion.and(TaskAttachment.TASK_UUID.eq(taskUuid),
                                TaskAttachment.DELETED_AT.eq(0))).limit(1));
        try {
            return files.getCount() > 0;
        } finally {
            files.close();
        }
    }

}

