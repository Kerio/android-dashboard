package com.kerio.dashboard.gui;
import java.security.KeyStoreException;
import java.security.cert.X509Certificate;

import com.kerio.dashboard.R;
import com.kerio.dashboard.api.TrustStoreHelper;
import com.kerio.dashboard.gui.tiles.CertificateTile;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;


@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class CertificateWarningDialog extends DialogFragment {

	private X509Certificate[] certChain = null;
	private TrustStoreHelper trustHelper = null;
	
	private TextView certificateView;
	private LinearLayout certificateLayout;
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
	    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	    
	    LayoutInflater inflater = getActivity().getLayoutInflater();

	    View root = inflater.inflate(R.layout.certificate_warning, null);
	    builder.setView(root);
	    builder.setTitle(R.string.cert_Warning_header);
	    builder.setMessage(R.string.cert_Warning_body);
	    
	    //TODO CIMA style the text better
	    TextView issuer = new TextView(root.getContext());
	    issuer.setText(R.string.cert_issuer);
	    issuer.setTextSize(16);
	    issuer.setTypeface(null, Typeface.BOLD);
	    boolean first = true;
	    
	    certificateLayout = (LinearLayout) root.findViewById(R.id.certificateLayout);
	    for(X509Certificate cert : this.certChain){
	    	if( ! first){
	    		certificateLayout.addView(issuer);
	    	}
	    	certificateLayout.addView(new CertificateTile(root.getContext(), cert));
	    	first = false;
	    	
	    }

	    // Add action buttons
	    builder.setNegativeButton(R.string.cert_Warning_reject, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
         	   CertificateWarningDialog.this.getDialog().cancel();
            }
        });
	    
	    builder.setPositiveButton(R.string.cert_Warning_trust, new DialogInterface.OnClickListener() {
           public void onClick(DialogInterface dialog, int id) {
        	   CertificateWarningDialog.this.trustCertificate();
           }
	    });
      
	    return builder.create();
	}
	
	private void trustCertificate() {
    	if(this.certChain != null && this.certChain.length >= 1){
    		try {
				this.trustHelper.getKeystore().setCertificateEntry("Trust-" + this.certChain[0].hashCode(), this.certChain[0]);
			} catch (KeyStoreException e1) {
				// TODO CIMA log at least
			}
		}
	}

	public X509Certificate[] getCertChain() {
		return certChain;
	}

	public void setCertChain(X509Certificate[] certChain) {
		this.certChain = certChain;
	}

	public TrustStoreHelper getTrustHelper() {
		return trustHelper;
	}

	public void setTrustHelper(TrustStoreHelper trustHelper) {
		this.trustHelper = trustHelper;
	}
}
