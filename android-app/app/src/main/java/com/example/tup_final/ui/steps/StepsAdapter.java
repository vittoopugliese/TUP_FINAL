package com.example.tup_final.ui.steps;

import android.app.DatePickerDialog;
import android.graphics.drawable.GradientDrawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.tup_final.R;
import com.example.tup_final.data.entity.ObservationEntity;
import com.example.tup_final.util.StepConstants;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Adapter para la lista de steps.
 * Usa view types por tipo de step (BINARY, DATE_RANGE, etc.).
 *
 * Fix de bucle infinito:
 *  - El Spinner ignora el primer onItemSelected (disparo inicial de Android).
 *  - El checkbox limpia su listener antes del setChecked programático.
 *  - updateObservations() usa notifyItemChanged con payload (no notifyDataSetChanged).
 */
public class StepsAdapter extends ListAdapter<StepUiModel, RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_BINARY = 0;
    private static final int VIEW_TYPE_DATE_RANGE = 1;
    private static final int VIEW_TYPE_SIMPLE_VALUE = 2;
    private static final int VIEW_TYPE_NUMERIC_RANGE = 3;
    private static final int VIEW_TYPE_MULTI_VALUE = 4;
    private static final int VIEW_TYPE_UNKNOWN = 5;

    /** Payload used for observation-only partial rebind (avoids full item rebind). */
    static final String PAYLOAD_OBS = "obs";

    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final long DEBOUNCE_MS = 400;

    private OnStepValueChangeListener onStepValueChangeListener;
    private OnAddObservationListener onAddObservationListener;
    private Map<String, List<ObservationEntity>> observationsMap = new HashMap<>();

    public interface OnStepValueChangeListener {
        void onStepValueChange(StepUiModel step, String valueJson, Boolean applicable);
    }

    public interface OnAddObservationListener {
        void onAddObservation(StepUiModel step);
    }

    public void setOnStepValueChangeListener(OnStepValueChangeListener listener) {
        this.onStepValueChangeListener = listener;
    }

    public void setOnAddObservationListener(OnAddObservationListener listener) {
        this.onAddObservationListener = listener;
    }

    /**
     * Actualiza el mapa de observaciones y notifica SOLO los items afectados,
     * sin forzar un rebind completo de todos los items (evita el bucle con el Spinner).
     */
    public void updateObservations(Map<String, List<ObservationEntity>> map) {
        if (map == null) return;
        this.observationsMap = new HashMap<>(map);
        List<StepUiModel> list = getCurrentList();
        for (int i = 0; i < list.size(); i++) {
            if (map.containsKey(list.get(i).id)) {
                notifyItemChanged(i, PAYLOAD_OBS);
            }
        }
    }

    public StepsAdapter() {
        super(new DiffUtil.ItemCallback<StepUiModel>() {
            @Override
            public boolean areItemsTheSame(@NonNull StepUiModel o, @NonNull StepUiModel n) {
                return o.id.equals(n.id);
            }

            @Override
            public boolean areContentsTheSame(@NonNull StepUiModel o, @NonNull StepUiModel n) {
                return (o.valueJson == null ? n.valueJson == null : o.valueJson.equals(n.valueJson))
                        && o.applicable == n.applicable
                        && o.status.equals(n.status);
            }
        });
    }

    @Override
    public int getItemViewType(int position) {
        switch (getItem(position).testStepType) {
            case StepConstants.TYPE_BINARY:       return VIEW_TYPE_BINARY;
            case StepConstants.TYPE_DATE_RANGE:   return VIEW_TYPE_DATE_RANGE;
            case StepConstants.TYPE_SIMPLE_VALUE: return VIEW_TYPE_SIMPLE_VALUE;
            case StepConstants.TYPE_NUMERIC_RANGE:
            case StepConstants.TYPE_RANGE:        return VIEW_TYPE_NUMERIC_RANGE;
            case StepConstants.TYPE_MULTI_VALUE:  return VIEW_TYPE_MULTI_VALUE;
            default:                              return VIEW_TYPE_UNKNOWN;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_step, parent, false);
        return new StepViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        StepUiModel step = getItem(position);
        List<ObservationEntity> obs = observationsMap.getOrDefault(step.id, Collections.emptyList());
        ((StepViewHolder) holder).bind(step, obs, onStepValueChangeListener, onAddObservationListener);
    }

    /**
     * Partial bind: if the payload is PAYLOAD_OBS, only update the observations section
     * without touching the input views (avoids retriggering Spinner / Checkbox listeners).
     */
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position,
                                 @NonNull List<Object> payloads) {
        if (!payloads.isEmpty() && PAYLOAD_OBS.equals(payloads.get(0))) {
            StepUiModel step = getItem(position);
            List<ObservationEntity> obs = observationsMap.getOrDefault(step.id, Collections.emptyList());
            ((StepViewHolder) holder).bindObservations(obs);
        } else {
            super.onBindViewHolder(holder, position, payloads);
        }
    }

    // ── ViewHolder ────────────────────────────────────────────────────────────

    static class StepViewHolder extends RecyclerView.ViewHolder {
        final MaterialCardView cardStep;
        final TextView textStepIndex;
        final TextView textStepName;
        final TextView textStepStatus;
        final com.google.android.material.card.MaterialCardView cardStep;
        final MaterialCheckBox checkNa;
        final ViewGroup containerInput;
        final LayoutInflater inflater;
        final LinearLayout containerObservations;
        final View dividerObservations;
        final MaterialButton btnAddObservation;

        StepViewHolder(@NonNull View itemView) {
            super(itemView);
            textStepIndex         = itemView.findViewById(R.id.text_step_index);
            textStepName          = itemView.findViewById(R.id.text_step_name);
            textStepStatus        = itemView.findViewById(R.id.text_step_status);
            cardStep              = (com.google.android.material.card.MaterialCardView) itemView;
            checkNa               = itemView.findViewById(R.id.check_step_na);
            containerInput        = itemView.findViewById(R.id.container_step_input);
            containerObservations = itemView.findViewById(R.id.container_observations);
            dividerObservations   = itemView.findViewById(R.id.divider_observations);
            btnAddObservation     = itemView.findViewById(R.id.btn_add_observation);
            inflater              = LayoutInflater.from(itemView.getContext());
        }

        void bind(StepUiModel step, List<ObservationEntity> observations,
                  OnStepValueChangeListener listener,
                  OnAddObservationListener obsListener) {

            textStepIndex.setText("#" + step.index);
            textStepName.setText(step.name);
            bindStatusBadge(step.status);

            // FIX: clear listener BEFORE setChecked to avoid firing the callback
            // with the programmatic value (would trigger updateStep unnecessarily).
            checkNa.setOnCheckedChangeListener(null);
            checkNa.setChecked(!step.applicable);
            checkNa.setOnCheckedChangeListener((btn, checked) -> {
                boolean applicable = !checked;
                containerInput.setEnabled(applicable);
                if (listener != null) {
                    listener.onStepValueChange(step, step.valueJson, applicable);
                }
            });

            containerInput.setEnabled(step.applicable);
            containerInput.removeAllViews();

            View inputView = inflateInputForType(step.testStepType);
            if (inputView != null) {
                containerInput.addView(inputView);
                bindInput(step, inputView, listener);
            }

            bindObservations(observations);

            btnAddObservation.setOnClickListener(v -> {
                if (obsListener != null) obsListener.onAddObservation(step);
            });
        }

        /** Muestra el badge de estado (FAILED/COMPLETED/PENDING) en la cabecera del step. */
        void bindStatusBadge(@Nullable String status) {
            android.content.Context ctx = itemView.getContext();
            int dpToPx1 = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 1f, ctx.getResources().getDisplayMetrics());

            if ("FAILED".equals(status)) {
                textStepStatus.setVisibility(View.VISIBLE);
                textStepStatus.setText("⚠ FALLIDO");
                int bgColor  = ctx.getResources().getColor(R.color.step_status_failed_bg,  null);
                int txtColor = ctx.getResources().getColor(R.color.step_status_failed_text, null);
                textStepStatus.setTextColor(txtColor);
                GradientDrawable badge = new GradientDrawable();
                badge.setShape(GradientDrawable.RECTANGLE);
                badge.setCornerRadius(12f * dpToPx1);
                badge.setColor(bgColor);
                textStepStatus.setBackground(badge);
                cardStep.setStrokeColor(ctx.getResources().getColor(R.color.step_status_failed_bg, null));
                cardStep.setStrokeWidth(2 * dpToPx1);

            } else if ("COMPLETED".equals(status)) {
                textStepStatus.setVisibility(View.VISIBLE);
                textStepStatus.setText("✓ OK");
                int bgColor  = ctx.getResources().getColor(R.color.step_status_completed_bg,  null);
                int txtColor = ctx.getResources().getColor(R.color.step_status_completed_text, null);
                textStepStatus.setTextColor(txtColor);
                GradientDrawable badge = new GradientDrawable();
                badge.setShape(GradientDrawable.RECTANGLE);
                badge.setCornerRadius(12f * dpToPx1);
                badge.setColor(bgColor);
                textStepStatus.setBackground(badge);
                cardStep.setStrokeColor(ctx.getResources().getColor(R.color.step_status_completed_bg, null));
                cardStep.setStrokeWidth(2 * dpToPx1);

            } else {
                textStepStatus.setVisibility(View.GONE);
                cardStep.setStrokeColor(android.graphics.Color.TRANSPARENT);
                cardStep.setStrokeWidth(0);
            }
        }

        // Called for partial updates (PAYLOAD_OBS) — does NOT touch input views.
        void bindObservations(List<ObservationEntity> observations) {
            containerObservations.removeAllViews();
            if (observations == null || observations.isEmpty()) {
                containerObservations.setVisibility(View.GONE);
                dividerObservations.setVisibility(View.GONE);
                return;
            }

            containerObservations.setVisibility(View.VISIBLE);
            dividerObservations.setVisibility(View.VISIBLE);

            for (ObservationEntity obs : observations) {
                View itemView = inflater.inflate(R.layout.item_observation, containerObservations, false);

                ImageView iconType = itemView.findViewById(R.id.icon_obs_type);
                TextView textType = itemView.findViewById(R.id.text_obs_type_badge);
                TextView textDesc = itemView.findViewById(R.id.text_obs_description);
                ImageView imageThumb = itemView.findViewById(R.id.image_obs_thumb);

                boolean isDeficiency = "DEFICIENCIES".equals(obs.type) || "DEFICIENCY".equals(obs.type);

                if (isDeficiency) {
                    iconType.setImageResource(android.R.drawable.ic_dialog_alert);
                    iconType.setColorFilter(
                            itemView.getContext().getResources().getColor(R.color.obs_deficiency_color, null));
                    textType.setText("DEFICIENCIA");
                    textType.setTextColor(
                            itemView.getContext().getResources().getColor(R.color.obs_deficiency_color, null));
                } else {
                    iconType.setImageResource(android.R.drawable.ic_dialog_info);
                    iconType.setColorFilter(
                            itemView.getContext().getResources().getColor(R.color.obs_remark_color, null));
                    textType.setText("OBSERVACIÓN");
                    textType.setTextColor(
                            itemView.getContext().getResources().getColor(R.color.obs_remark_color, null));
                }

                textDesc.setText(obs.description != null ? obs.description : "");

                if (obs.mediaId != null && !obs.mediaId.isEmpty()) {
                    imageThumb.setVisibility(View.VISIBLE);
                    Glide.with(itemView.getContext())
                            .load(obs.mediaId)
                            .centerCrop()
                            .into(imageThumb);
                } else {
                    imageThumb.setVisibility(View.GONE);
                }

                containerObservations.addView(itemView);
            }
        }

        // ── Input inflaters ───────────────────────────────────────────────────

        private View inflateInputForType(String type) {
            switch (type) {
                case StepConstants.TYPE_BINARY:
                    return inflater.inflate(R.layout.input_step_binary, containerInput, false);
                case StepConstants.TYPE_DATE_RANGE:
                    return inflater.inflate(R.layout.input_step_date_range, containerInput, false);
                case StepConstants.TYPE_SIMPLE_VALUE:
                    return inflater.inflate(R.layout.input_step_simple_value, containerInput, false);
                case StepConstants.TYPE_NUMERIC_RANGE:
                case StepConstants.TYPE_RANGE:
                    return inflater.inflate(R.layout.input_step_numeric_range, containerInput, false);
                case StepConstants.TYPE_MULTI_VALUE:
                    return inflater.inflate(R.layout.input_step_multi_value, containerInput, false);
                default:
                    return inflater.inflate(R.layout.input_step_simple_value, containerInput, false);
            }
        }

        private void bindInput(StepUiModel step, View inputView, OnStepValueChangeListener listener) {
            switch (step.testStepType) {
                case StepConstants.TYPE_BINARY:
                    bindBinary(step, inputView, listener);
                    break;
                case StepConstants.TYPE_DATE_RANGE:
                    bindDateRange(step, inputView, listener);
                    break;
                case StepConstants.TYPE_SIMPLE_VALUE:
                    bindSimpleValue(step, inputView, listener);
                    break;
                case StepConstants.TYPE_NUMERIC_RANGE:
                case StepConstants.TYPE_RANGE:
                    bindNumericRange(step, inputView, listener);
                    break;
                case StepConstants.TYPE_MULTI_VALUE:
                    bindMultiValue(step, inputView, listener);
                    break;
                default:
                    bindSimpleValue(step, inputView, listener);
            }
        }

        /**
         * Binds the binary (Yes/No) spinner.
         *
         * FIX: Android's Spinner always fires onItemSelected once after its first layout,
         * even when the selection was set programmatically before the listener was attached.
         * We use an 'isFirstCall' flag to silently skip that initial callback so it does
         * NOT trigger updateStep() unnecessarily and cause an infinite refresh loop.
         */
        private void bindBinary(StepUiModel step, View v, OnStepValueChangeListener listener) {
            Spinner spinner = v.findViewById(R.id.spinner_binary);
            TextView errorText = v.findViewById(R.id.text_binary_error);

            String[] options = itemView.getContext().getResources()
                    .getStringArray(R.array.step_binary_options);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(itemView.getContext(),
                    android.R.layout.simple_spinner_item, options);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);

            Boolean current = StepValueMapper.parseBinaryValue(step.valueJson);
            int pos = current == null ? 0 : (current ? 1 : 2);
            spinner.setSelection(pos);

            // Show/hide error for the initial state without triggering the listener.
            if (current != null) {
                errorText.setVisibility(View.GONE);
            } else if (step.applicable) {
                errorText.setVisibility(View.VISIBLE);
                errorText.setText(itemView.getContext().getString(R.string.step_error_required));
            }

            final boolean[] isFirstCall = {true};

            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    // Skip the very first call: Android fires this after layout even
                    // though the selection was already set programmatically above.
                    if (isFirstCall[0]) {
                        isFirstCall[0] = false;
                        return;
                    }

                    if (position == 0) {
                        errorText.setVisibility(View.VISIBLE);
                        errorText.setText(itemView.getContext().getString(R.string.step_error_required));
                        if (listener != null) listener.onStepValueChange(step, null, step.applicable);
                    } else {
                        errorText.setVisibility(View.GONE);
                        Boolean val = position == 1;
                        String json = StepValueMapper.toBinaryJson(val);
                        if (listener != null) listener.onStepValueChange(step, json, step.applicable);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
        }

        private void bindDateRange(StepUiModel step, View v, OnStepValueChangeListener listener) {
            TextInputEditText fromInput = v.findViewById(R.id.input_date_from);
            TextInputEditText toInput = v.findViewById(R.id.input_date_to);
            TextView errorText = v.findViewById(R.id.text_date_range_error);

            String[] parsed = StepValueMapper.parseDateRangeValue(step.valueJson);
            String from = parsed != null && parsed.length > 0 ? parsed[0] : "";
            String to = parsed != null && parsed.length > 1 ? parsed[1] : "";
            fromInput.setText(from);
            toInput.setText(to);

            Runnable pickFrom = () -> showDatePicker(fromInput, toInput, errorText, step, listener, true);
            Runnable pickTo = () -> showDatePicker(fromInput, toInput, errorText, step, listener, false);

            fromInput.setOnClickListener(v1 -> pickFrom.run());
            toInput.setOnClickListener(v1 -> pickTo.run());

            updateDateRangeError(fromInput, toInput, errorText);
            notifyDateRangeIfValid(fromInput.getText().toString(), toInput.getText().toString(),
                    step, listener, errorText);
        }

        private void showDatePicker(TextInputEditText fromInput, TextInputEditText toInput,
                                    TextView errorText, StepUiModel step,
                                    OnStepValueChangeListener listener, boolean isFrom) {
            Calendar cal = Calendar.getInstance();
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.US);
                String str = isFrom ? fromInput.getText().toString() : toInput.getText().toString();
                if (str != null && !str.isEmpty()) cal.setTime(sdf.parse(str));
            } catch (Exception ignored) {}
            new DatePickerDialog(itemView.getContext(),
                    (view, year, month, day) -> {
                        cal.set(year, month, day);
                        String dateStr = new SimpleDateFormat(DATE_FORMAT, Locale.US).format(cal.getTime());
                        if (isFrom) fromInput.setText(dateStr);
                        else toInput.setText(dateStr);
                        updateDateRangeError(fromInput, toInput, errorText);
                        notifyDateRangeIfValid(fromInput.getText().toString(),
                                toInput.getText().toString(), step, listener, errorText);
                    },
                    cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
            ).show();
        }

        private void updateDateRangeError(TextInputEditText from, TextInputEditText to,
                                          TextView errorText) {
            String fromStr = from.getText().toString().trim();
            String toStr = to.getText().toString().trim();
            if (fromStr.isEmpty() || toStr.isEmpty()) {
                errorText.setVisibility(View.GONE);
                return;
            }
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.US);
                Date dFrom = sdf.parse(fromStr);
                Date dTo = sdf.parse(toStr);
                if (dFrom != null && dTo != null && dFrom.after(dTo)) {
                    errorText.setText(itemView.getContext().getString(R.string.step_error_date_range));
                    errorText.setVisibility(View.VISIBLE);
                } else {
                    errorText.setVisibility(View.GONE);
                }
            } catch (Exception e) {
                errorText.setVisibility(View.GONE);
            }
        }

        private void notifyDateRangeIfValid(String from, String to, StepUiModel step,
                                            OnStepValueChangeListener listener, TextView errorText) {
            if (from.isEmpty() && to.isEmpty()) {
                if (listener != null) listener.onStepValueChange(step, null, step.applicable);
                return;
            }
            if (from.isEmpty() || to.isEmpty()) {
                if (listener != null) listener.onStepValueChange(step,
                        StepValueMapper.toDateRangeJson(from, to), step.applicable);
                return;
            }
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.US);
                Date dFrom = sdf.parse(from);
                Date dTo = sdf.parse(to);
                if (dFrom != null && dTo != null && !dFrom.after(dTo) && listener != null) {
                    listener.onStepValueChange(step, StepValueMapper.toDateRangeJson(from, to),
                            step.applicable);
                }
            } catch (Exception ignored) {}
        }

        private void bindSimpleValue(StepUiModel step, View v, OnStepValueChangeListener listener) {
            TextInputEditText input = v.findViewById(R.id.input_simple);
            TextView errorText = v.findViewById(R.id.text_simple_error);
            String current = StepValueMapper.parseStringValue(step.valueJson);
            input.setText(current != null ? current : "");
            if (step.applicable && (current == null || current.trim().isEmpty())) {
                errorText.setVisibility(View.VISIBLE);
                errorText.setText(itemView.getContext().getString(R.string.step_error_required));
            } else {
                errorText.setVisibility(View.GONE);
            }

            android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
            Runnable pending = () -> {
                String val = input.getText().toString().trim();
                if (step.applicable && val.isEmpty()) {
                    errorText.setVisibility(View.VISIBLE);
                    errorText.setText(itemView.getContext().getString(R.string.step_error_required));
                    if (listener != null) listener.onStepValueChange(step, null, step.applicable);
                } else {
                    errorText.setVisibility(View.GONE);
                    if (listener != null)
                        listener.onStepValueChange(step, StepValueMapper.toStringJson(val), step.applicable);
                }
            };
            input.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int i, int c, int a) {}
                @Override public void onTextChanged(CharSequence s, int i, int b, int c) {}
                @Override
                public void afterTextChanged(Editable s) {
                    handler.removeCallbacks(pending);
                    handler.postDelayed(pending, DEBOUNCE_MS);
                }
            });
        }

        private void bindNumericRange(StepUiModel step, View v, OnStepValueChangeListener listener) {
            TextInputEditText input = v.findViewById(R.id.input_numeric);
            TextInputLayout til = v.findViewById(R.id.til_numeric);
            TextView errorText = v.findViewById(R.id.text_numeric_error);

            Double min = step.minValue;
            Double max = step.maxValue;
            if (min != null && max != null) til.setSuffixText(" (" + min + "-" + max + ")");

            Double current = StepValueMapper.parseNumericValue(step.valueJson);
            input.setText(current != null ? String.valueOf(current) : "");

            android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
            Runnable validateAndNotify = () -> {
                String raw = input.getText().toString().trim();
                if (raw.isEmpty()) {
                    errorText.setVisibility(step.applicable ? View.VISIBLE : View.GONE);
                    errorText.setText(itemView.getContext().getString(R.string.step_error_required));
                    if (listener != null) listener.onStepValueChange(step, null, step.applicable);
                    return;
                }
                try {
                    double val = Double.parseDouble(raw);
                    if (min != null && val < min) {
                        errorText.setText(itemView.getContext().getString(
                                R.string.step_error_numeric_range, min, max != null ? max : min));
                        errorText.setVisibility(View.VISIBLE);
                        if (listener != null)
                            listener.onStepValueChange(step, StepValueMapper.toNumericJson(val), step.applicable);
                    } else if (max != null && val > max) {
                        errorText.setText(itemView.getContext().getString(
                                R.string.step_error_numeric_range, min != null ? min : max, max));
                        errorText.setVisibility(View.VISIBLE);
                        if (listener != null)
                            listener.onStepValueChange(step, StepValueMapper.toNumericJson(val), step.applicable);
                    } else {
                        errorText.setVisibility(View.GONE);
                        if (listener != null)
                            listener.onStepValueChange(step, StepValueMapper.toNumericJson(val), step.applicable);
                    }
                } catch (NumberFormatException e) {
                    errorText.setVisibility(View.VISIBLE);
                    errorText.setText(itemView.getContext().getString(R.string.step_error_required));
                }
            };

            input.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int i, int c, int a) {}
                @Override public void onTextChanged(CharSequence s, int i, int b, int c) {}
                @Override
                public void afterTextChanged(Editable s) {
                    handler.removeCallbacks(validateAndNotify);
                    handler.postDelayed(validateAndNotify, DEBOUNCE_MS);
                }
            });
            validateAndNotify.run();
        }

        private void bindMultiValue(StepUiModel step, View v, OnStepValueChangeListener listener) {
            TextInputEditText i1 = v.findViewById(R.id.input_multi_1);
            TextInputEditText i2 = v.findViewById(R.id.input_multi_2);
            TextInputEditText i3 = v.findViewById(R.id.input_multi_3);
            TextView errorText = v.findViewById(R.id.text_multi_error);

            String[] parsed = StepValueMapper.parseMultiValue(step.valueJson);
            if (parsed != null) {
                i1.setText(parsed.length > 0 ? parsed[0] : "");
                i2.setText(parsed.length > 1 ? parsed[1] : "");
                i3.setText(parsed.length > 2 ? parsed[2] : "");
            }

            android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
            Runnable[] pendingRef = new Runnable[1];
            Runnable notify = () -> {
                String v1 = i1.getText().toString().trim();
                String v2 = i2.getText().toString().trim();
                String v3 = i3.getText().toString().trim();
                boolean allEmpty = v1.isEmpty() && v2.isEmpty() && v3.isEmpty();
                boolean anyEmpty = v1.isEmpty() || v2.isEmpty() || v3.isEmpty();
                if (step.applicable && allEmpty) {
                    errorText.setVisibility(View.VISIBLE);
                    errorText.setText(itemView.getContext().getString(R.string.step_error_multi_incomplete));
                    if (listener != null) listener.onStepValueChange(step, null, step.applicable);
                } else if (step.applicable && anyEmpty) {
                    errorText.setVisibility(View.VISIBLE);
                    errorText.setText(itemView.getContext().getString(R.string.step_error_multi_incomplete));
                    if (listener != null)
                        listener.onStepValueChange(step, StepValueMapper.toMultiValueJson(v1, v2, v3), step.applicable);
                } else {
                    errorText.setVisibility(View.GONE);
                    if (listener != null)
                        listener.onStepValueChange(step, StepValueMapper.toMultiValueJson(v1, v2, v3), step.applicable);
                }
            };

            TextWatcher watcher = new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int i, int c, int a) {}
                @Override public void onTextChanged(CharSequence s, int i, int b, int c) {}
                @Override
                public void afterTextChanged(Editable s) {
                    if (pendingRef[0] != null) handler.removeCallbacks(pendingRef[0]);
                    pendingRef[0] = notify;
                    handler.postDelayed(notify, DEBOUNCE_MS);
                }
            };
            i1.addTextChangedListener(watcher);
            i2.addTextChangedListener(watcher);
            i3.addTextChangedListener(watcher);
        }
    }
}
