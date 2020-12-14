package com.example.filtros

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import com.example.filtros.Interface.EditImageFragmentListener
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.fragment_edit_image.*

class EditImageFragment : BottomSheetDialogFragment(), SeekBar.OnSeekBarChangeListener {

    private var listener:EditImageFragmentListener? = null
    var seekbar_brightness: SeekBar? = null
    var seekbar_saturation: SeekBar? = null
    var seekbar_constrant: SeekBar? = null

    companion object {
        internal var instance: EditImageFragment? = null

        fun getInstance(): EditImageFragment {
            if (instance == null) {
                instance = EditImageFragment()
            }
            return instance !!
        }
    }

    fun resetControls() {
        seekbar_brightness!!.progress = 100
        seekbar_constrant!!.progress = 0
        seekbar_saturation!!.progress = 10
    }

    fun setListener(listener: EditImageFragmentListener) {
        this.listener = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_edit_image, container, false)

        seekbar_brightness = view.findViewById<SeekBar>(R.id.seekbar_brightness)
        seekbar_saturation = view.findViewById<SeekBar>(R.id.seekbar_saturation)
        seekbar_constrant = view.findViewById<SeekBar>(R.id.seekbar_constrant)

        seekbar_brightness!!.max = 200
        seekbar_brightness!!.progress = 100

        seekbar_constrant!!.max = 20
        seekbar_constrant!!.progress = 0

        seekbar_saturation!!.max = 30
        seekbar_saturation!!.progress = 10

        seekbar_brightness!!.setOnSeekBarChangeListener(this)
        seekbar_constrant!!.setOnSeekBarChangeListener(this)
        seekbar_saturation!!.setOnSeekBarChangeListener(this)

        return view
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        var progress = progress
        if (listener != null) {
            if (seekBar?.id   == R.id.seekbar_brightness) {
                listener!!.onBrightnessChanged(progress - 100)
            }
            else if (seekBar?.id == R.id.seekbar_constrant) {
                progress += 10
                val  floatVal: Float = .10f*progress
                listener!!.onConstrantChanged(floatVal)
            }
            else if (seekBar?.id == R.id.seekbar_saturation) {
                val floatVal: Float = .10f*progress
                listener!!.onSaturationChanged(floatVal)
            }
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
        if (listener != null) {
            listener!!.onEditStarted()
        }
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
        if (listener != null) {
            listener!!.onEditCompleted()
        }
    }

}