package cn.eas.usdk.demo.util;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.List;

import cn.eas.usdk.demo.R;

public final class DialogUtil {

	private static int selectPosition;
	
	private DialogUtil() {}

	public static void showSelectDialog(Context context, String tittle, List<String> dataList, int checkedItem, final OnSelectListener listener) {
		selectPosition = checkedItem;

		final Builder b = new Builder(context);
		final AlertDialog dlg = b.create();
		dlg.setCancelable(false);
		dlg.show();
		dlg.setContentView(R.layout.select_dialog_view);

		TextView header = (TextView)dlg.findViewById(R.id.tvHeader);
		header.setText(tittle);
		Button btnCancel = (Button)dlg.findViewById(R.id.btnCancel);
		btnCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dlg.dismiss();
				listener.onCancel();
			}
		});

		final ListView list = (ListView) dlg.findViewById(R.id.listView);
		final ExBaseAdapter adapter = new ExBaseAdapter(context, dataList);
		list.setAdapter(adapter);
		list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, final int position, long id) {
				selectPosition = position;
				adapter.notifyDataSetChanged();
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						dlg.dismiss();
						listener.onSelected(position);
					}
				}, 50);
			}
		});
	}

	public static AlertDialog showMessage(Context context, String title, String message) {
		if(context == null) {
			return null;
		}
		final AlertDialog dialog = instanceDialog(context, title, message);
		Button btnConfirm = (Button) dialog.findViewById(R.id.ok_btn);
		btnConfirm.setVisibility(View.GONE);
		Button btnCancel = (Button) dialog.findViewById(R.id.cancel_btn);
		btnCancel.setVisibility(View.GONE);

		((LinearLayout)dialog.findViewById(R.id.layoutBtn)).setVisibility(View.GONE);
		((View)dialog.findViewById(R.id.viewLine)).setVisibility(View.GONE);
		return dialog;
	}

	public static AlertDialog showMessage(Context context, String title, String message, final OnConfirmListener listener) {
		if(context == null) {
			return null;
		}
		final AlertDialog dialog = instanceDialog(context, title, message);
		Button btnConfirm = (Button) dialog.findViewById(R.id.ok_btn);
		btnConfirm.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
				if (listener != null) {
					listener.onConfirm();
				}
			}
		});
		Button btnCancel = (Button) dialog.findViewById(R.id.cancel_btn);
		btnCancel.setVisibility(View.GONE);
		return dialog;
	}

	public static AlertDialog showMessage(Context context, String title, String message, final OnCancelListener listener) {
		if (context == null) {
			return null;
		}
		final AlertDialog dialog = instanceDialog(context, title, message);
		Button btnConfirm = (Button) dialog.findViewById(R.id.ok_btn);
		btnConfirm.setVisibility(View.GONE);

		Button btnCancel = (Button) dialog.findViewById(R.id.cancel_btn);
		btnCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
				if (listener != null) {
					listener.onCancel();
				}
			}
		});
		return dialog;
	}

	public static AlertDialog showOption(Context context, String title, String message, final OnChooseListener listener) {
		return showOption(context, title, message, null, null, listener);
	}

	public static AlertDialog showOption(Context context, String title, String message, String okBtnText, String cancelBtnText, final OnChooseListener listener) {
		final AlertDialog dialog = instanceDialog(context, title, message );
		Button btnConfirm = (Button) dialog.findViewById(R.id.ok_btn);
		if (okBtnText != null){
			btnConfirm.setText(okBtnText);
		}
		btnConfirm.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
				if (listener != null) {
					listener.onConfirm();
				}
			}
		});
		Button btnCancel = (Button) dialog.findViewById(R.id.cancel_btn);
		if (cancelBtnText != null){
			btnCancel.setText(cancelBtnText);
		}
		btnCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
				if (listener != null) {
					listener.onCancel();
				}
			}
		});
		return dialog;
	}

	public static AlertDialog showProgress(Context context, String title, String message) {
		final AlertDialog dialog = instanceDialog(context, title, message );
		LinearLayout layoutBtn = (LinearLayout) dialog.findViewById(R.id.layoutBtn);
		layoutBtn.setVisibility(View.GONE);
		return dialog;
	}

	private static AlertDialog instanceDialog(Context context, String title, String message ) {
		final AlertDialog dialog = new Builder(context).create();
		dialog.setCancelable(false);
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();
		dialog.setContentView(R.layout.dialog_choose);
		
		TextView tvTitle = (TextView) dialog.findViewById(R.id.title_tv);
		if (TextUtils.isEmpty(title)) {
			tvTitle.setVisibility(View.GONE);
		} else {
			tvTitle.setText(title);
		}
		TextView tvContent = (TextView) dialog.findViewById(R.id.message_tv);
		tvContent.setText(message);
		
		return dialog;
	}


	public interface OnSelectListener {
		void onCancel();
		void onSelected(int item);
	}

	/**
	 * ????????????
	 * @author baoxl
	 *
	 */
	public interface OnChooseListener {
		void onConfirm();
		void onCancel();
	}
	
	/**
	 * ??????????????????
	 * @author baoxl
	 *
	 */
	public interface OnConfirmListener {
		void onConfirm();
	}

	public interface OnCancelListener {
		void onCancel();
	}

	private static class ExBaseAdapter extends BaseAdapter {
		private Context context;
		private List<String> dataList;
		private LayoutInflater inflater;

		public ExBaseAdapter(Context ctx, List<String> data) {
			context = ctx;
			dataList = data;
			inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			return dataList.size();
		}

		@Override
		public Object getItem(int position) {
			return dataList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = inflater.inflate(R.layout.select_dialog_item, null);
				holder.info = (RadioButton) convertView.findViewById(R.id.info);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder)convertView.getTag();
			}
			if (selectPosition == position) {
				holder.info.setChecked(true);
			} else{
				holder.info.setChecked(false);
			}

			holder.info.setText(dataList.get(position));
			return convertView;
		}
	}

	private static class ViewHolder {
		public RadioButton info;
	}
}
