package com.example.APBook.presentation.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;
import com.example.APBook.R;
import com.example.APBook.domain.models.projects.ProjectModel;
import com.example.APBook.presentation.fragments.projects.MyProjectInfoFragment;

import java.util.List;

public class MyProjectsAdapter extends ArrayAdapter<ProjectModel> {
    private LayoutInflater inflater;
    private List<ProjectModel> listItemAd;
    private Context context;
    ProjectModel listItemUser;
    FragmentManager fragmentManager;

    public MyProjectsAdapter(@NonNull Context context, int resource, List<ProjectModel> listItemAd, LayoutInflater inflater, FragmentManager fragmentManager) {
        super(context, resource, listItemAd);
        this.inflater = inflater;
        this.listItemAd = listItemAd;
        this.context = context;
        this.fragmentManager = fragmentManager;
    }

    @SuppressLint("ViewHolder")
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        MyProjectsAdapter.ViewHolder viewHolder;
        listItemUser = listItemAd.get(position);
        convertView = inflater.inflate(R.layout.item_project, null, false);
        viewHolder = new MyProjectsAdapter.ViewHolder();
        viewHolder.logo = convertView.findViewById(R.id.post_image);
        Glide.with(context)
                .load(listItemUser.logo)
                .placeholder(R.drawable.baseline_work_24)
                .into(viewHolder.logo);
        viewHolder.projectName = convertView.findViewById(R.id.project_name_textview);
        viewHolder.description = convertView.findViewById(R.id.description);
        viewHolder.projectName.setText(listItemUser.name);
        viewHolder.description.setText(listItemUser.description);

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProjectModel project = listItemAd.get(position);
                MyProjectInfoFragment projectInfoFragment = new MyProjectInfoFragment(project);
                fragmentManager.beginTransaction()
                        .replace(R.id.flFragment, projectInfoFragment)
                        .commit();
            }

        });

        return convertView;
    }

    private class ViewHolder {
        TextView projectName, description;
        ImageView logo;
    }
}
