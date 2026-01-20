package com.easyfitness.machines;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.easyfitness.DAO.DAOMachine;
import com.easyfitness.DAO.Machine;
import com.easyfitness.R;
import com.easyfitness.utils.ImageUtil;
import com.github.ivbaranov.mfb.MaterialFavoriteButton;

public class MachineCursorAdapter extends CursorAdapter implements Filterable {

    private final DAOMachine mDbMachine;
    private final LayoutInflater mInflater;

    public MachineCursorAdapter(Context context, Cursor c, int flags, DAOMachine dbMachine) {
        super(context, c, flags);
        this.mDbMachine = dbMachine;
        this.mInflater = LayoutInflater.from(context);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        TextView t0 = view.findViewById(R.id.LIST_MACHINE_ID);
        t0.setText(cursor.getString(cursor.getColumnIndexOrThrow(DAOMachine.KEY)));

        TextView t1 = view.findViewById(R.id.LIST_MACHINE_NAME);
        t1.setText(cursor.getString(cursor.getColumnIndexOrThrow(DAOMachine.NAME)));

        TextView t2 = view.findViewById(R.id.LIST_MACHINE_SHORT_DESCRIPTION);
        t2.setText(cursor.getString(cursor.getColumnIndexOrThrow(DAOMachine.DESCRIPTION)));

        ImageView imageView = view.findViewById(R.id.LIST_MACHINE_PHOTO);
        String imagePath = cursor.getString(cursor.getColumnIndexOrThrow(DAOMachine.PICTURE));
        int type = cursor.getInt(cursor.getColumnIndexOrThrow(DAOMachine.TYPE));

        if (imagePath != null && !imagePath.isEmpty()) {
            ImageUtil.setThumb(imageView, imagePath);
        } else {
            imageView.setImageResource(
                type == DAOMachine.TYPE_FONTE
                    ? R.drawable.ic_machine
                    : R.drawable.ic_running
            );
        }

        MaterialFavoriteButton favButton = view.findViewById(R.id.LIST_MACHINE_FAVORITE);
        boolean isFavorite = cursor.getInt(cursor.getColumnIndexOrThrow(DAOMachine.FAVORITE)) == 1;

        favButton.setFavorite(isFavorite);
        favButton.setAnimateFavorite(true);
        favButton.setRotationDuration(500);
        favButton.setTag(cursor.getLong(cursor.getColumnIndexOrThrow(DAOMachine.KEY)));

        favButton.setOnClickListener(v -> {
            MaterialFavoriteButton btn = (MaterialFavoriteButton) v;
            boolean current = btn.isFavorite();
            btn.setFavoriteAnimated(!current);

            if (mDbMachine != null) {
                long machineId = (long) btn.getTag();
                Machine machine = mDbMachine.getMachine(machineId);
                machine.setFavorite(!current);
                mDbMachine.updateMachine(machine);
            }
        });
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return mInflater.inflate(R.layout.machinelist_row, parent, false);
    }
}
