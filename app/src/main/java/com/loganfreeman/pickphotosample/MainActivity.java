package com.loganfreeman.pickphotosample;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.werb.permissionschecker.PermissionChecker;
import com.werb.pickphotoview.PickPhotoView;
import com.werb.pickphotoview.adapter.SpaceItemDecoration;
import com.werb.pickphotoview.util.PickConfig;
import com.werb.pickphotoview.util.PickUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static android.R.attr.path;

public class MainActivity extends AppCompatActivity {

    private SampleAdapter adapter;
    private PermissionChecker permissionChecker;
    private String[] PERMISSIONS = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        permissionChecker = new PermissionChecker(this);

        findViewById(R.id.click).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSelectPhotoClick();
            }
        });

        findViewById(R.id.share).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                shareSelectedPhotos(adapter.getImagePaths());
            }
        });

        findViewById(R.id.share_to_moments).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ArrayList<Uri> uris = new ArrayList<Uri>();
                for(String path: adapter.getImagePaths()) {
                    uris.add(getUri(path));
                }
                shareToTimeLine(uris);
            }
        });

        findViewById(R.id.share_to_friend).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                shareToFriend(adapter.getImagePaths().get(0));
            }
        });

        onSelectPhotoClick();

        RecyclerView photoList = (RecyclerView) findViewById(R.id.photo_list);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 4);
        photoList.setLayoutManager(layoutManager);
        photoList.addItemDecoration(new SpaceItemDecoration(PickUtils.getInstance(MainActivity.this).dp2px(PickConfig.ITEM_SPACE), 4));
        adapter = new SampleAdapter(this,null);
        photoList.setAdapter(adapter);
    }




    private void onSelectPhotoClick() {
        if(permissionChecker.isLackPermissions(PERMISSIONS)){
            permissionChecker.requestPermissions();
        }else {
            startPickPhoto();
        }
    }

    private void startPickPhoto(){
        new PickPhotoView.Builder(MainActivity.this)
                .setPickPhotoSize(9)
                .setShowCamera(true)
                .setSpanCount(5)
                .setLightStatusBar(true)
                .setStatusBarColor("#ffffff")
                .setToolbarColor("#ffffff")
                .setToolbarIconColor("#000000")
                .start();
    }

    private Uri getUri(String path) {
        return FileProvider.getUriForFile(this, this.getApplicationContext().getPackageName() + ".provider", new File(path));
    }

    private void shareSelectedPhotos(List<String> filesToSend) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND_MULTIPLE);
        intent.putExtra(Intent.EXTRA_SUBJECT, "Here are some files.");
        intent.setType("image/jpeg"); /* This example is sharing jpeg images. */

        ArrayList<Uri> files = new ArrayList<Uri>();

        for(String path : filesToSend /* List of the files you want to send */) {

            files.add(getUri(path));
        }

        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == 0){
            return;
        }
        if(data == null){
            return;
        }
        if (requestCode == PickConfig.PICK_PHOTO_DATA) {
            List<String> selectPaths = (List<String>) data.getSerializableExtra(PickConfig.INTENT_IMG_LIST_SELECT);
            adapter.updateData(selectPaths);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PermissionChecker.PERMISSION_REQUEST_CODE:
                if (permissionChecker.hasAllPermissionsGranted(grantResults)) {
                    startPickPhoto();
                } else {
                    permissionChecker.showDialog();
                }
                break;
        }
    }


    public static boolean isInstallWeChart(Context context){

        PackageInfo packageInfo = null;

        try {

            packageInfo = context.getPackageManager().getPackageInfo("com.tencent.mm", 0);

        } catch (Exception e) {

            packageInfo = null;

            e.printStackTrace();

        }

        if (packageInfo == null) {

            return false;

        } else {

            return true;

        }

    }

    /**
     * 分享图片给好友
     *
     * @param path
     */
    private void shareToFriend(String path) {
        Intent intent = new Intent();
        ComponentName comp = new ComponentName("com.tencent.mm", "com.tencent.mm.ui.tools.ShareImgUI");
        intent.setComponent(comp);
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_STREAM, getUri(path));
        startActivity(intent);
    }


    /**
     * 分享多图到朋友圈，多张图片加文字
     *
     * @param uris
     */
    private void shareToTimeLine(ArrayList<Uri> uris) {
        Intent intent = new Intent();
        ComponentName comp = new ComponentName("com.tencent.mm", "com.tencent.mm.ui.tools.ShareToTimeLineUI");
        intent.setComponent(comp);
        intent.setAction(Intent.ACTION_SEND_MULTIPLE);
        intent.setType("image/*");

        intent.putExtra("Kdescription", "分享多张图片到朋友圈");

        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        startActivity(intent);
    }








}
