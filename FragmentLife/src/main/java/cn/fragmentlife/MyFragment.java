package cn.fragmentlife;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/*Fragment生命周期的总结
 * 1：程序启动后，先调用onAttach、onCreate、onCreateView三个方法
 * 2：然后Activity 调用 onCreate方法完成后，Fragment再调用onActivityCreated
 * 3：然后Activity 调用 onStart方法完成后，Fragment再调用onStart
 * 4：然后Activity 调用 onResume方法完成后，Fragment再调用onResume
 * 5：程序退出时，Fragment调用onPause，然后然后Activity 调用 onPause方法
 * 6：然后Fragment调用onStop，然后然后Activity 调用 onStop方法
 * 7：然后Fragment调用onDestoryView、onDestory、onDetach
 * 8：最后Activity调用onDestory
 * 9：Fragment创建时必须要含有一个View控件，可以在onCreateView()方法里创建一个View
 * 10：可以在onActivityCreated()方法里进行Fragment的初始化
 */

public class MyFragment extends Fragment {

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        System.out.println("Fragment   onAttach");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("Fragment   onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        TextView tv = new TextView(getActivity());
        tv.setText("test");
        System.out.println("Fragment  onCreateView ");
        return tv;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        System.out.println("Fragment  onActivityCreated ");
    }

    @Override
    public void onStart() {
        super.onStart();
        System.out.println("Fragment  onStart ");
    }

    @Override
    public void onResume() {
        super.onResume();
        System.out.println("Fragment   onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        System.out.println("Fragment   onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        System.out.println("Fragment   onStop");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        System.out.println("Fragment  onDestroyView ");
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
        System.out.println("Fragment   onDestroy");
    }

    @Override
    public void onDetach() {

        super.onDetach();
        System.out.println("Fragment  onDetach ");
    }

}
