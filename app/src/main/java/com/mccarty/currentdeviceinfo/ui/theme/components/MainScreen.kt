package com.mccarty.currentdeviceinfo.ui.theme.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFromBaseline
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mccarty.currentdeviceinfo.MainActivity
import com.mccarty.currentdeviceinfo.R

@Composable
fun MainScreen(
    currentLocalTime: String?,
    currentUtcTime: String?,
    wifiIpAddress: String?,
    cellIpAddress: String?,
    currentPosition: MainActivity.Position,
    onClick: () -> Unit,
) {
    val containerPadding = 24.dp
    val textString = MaterialTheme.typography.titleMedium

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.onSurface)
            .padding(24.dp)
    ) {


        item {
            DeviceInfo(
                containerPadding,
                textString,
                firstLabel = stringResource(id = R.string.local_time_label),
                secondLabel = stringResource(id = R.string.utc_time_label),
                firstInfoLine = currentLocalTime,
                secondInfoLline = currentUtcTime,
            )

            DeviceInfo(
                containerPadding,
                textString,
                stringResource(id = R.string.lat_label),
                stringResource(id = R.string.lon_label),
                currentPosition.lat.toString(),
                currentPosition.lon.toString(),
            )

            DeviceInfo(
                containerPadding,
                textString,
                firstLabel = stringResource(id = R.string.wifi_ip_address_label),
                secondLabel = stringResource(id = R.string.cellular_ip_address_label),
                firstInfoLine = wifiIpAddress,
                secondInfoLline = cellIpAddress,
            )

            SubmitButton(
                name = stringResource(id = R.string.button_export),
                onClick = { onClick() })
        }
    }
}

@Composable
fun DeviceInfo(
    padding: Dp,
    textStyle: TextStyle,
    firstLabel: String,
    secondLabel: String,
    firstInfoLine: String?,
    secondInfoLline: String?,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(padding)
    ) {
        Row {
            Column(Modifier.weight(1f)) {
                Text(
                    text = firstLabel,
                    modifier = Modifier
                        .paddingFromBaseline(top = 25.dp)
                        .padding(start = 16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    style = textStyle,
                )
                Text(
                    text = secondLabel,
                    modifier = Modifier
                        .paddingFromBaseline(top = 25.dp)
                        .padding(start = 16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    style = textStyle,
                )
            }

            Column(Modifier.weight(1f)) {
                Text(
                    text = firstInfoLine ?: "not connected",
                    modifier = Modifier
                        .paddingFromBaseline(top = 25.dp)
                        .padding(start = 16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    style = textStyle,
                )
                Text(
                    text = secondInfoLline ?: "not connected",
                    modifier = Modifier
                        .paddingFromBaseline(top = 25.dp)
                        .padding(start = 16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    style = textStyle,
                )
            }
        }
    }
}

@Composable
fun SubmitButton(name: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth(),
    ) {
        OutlinedButton(
            modifier = Modifier
                .fillMaxWidth()
                .width(dimensionResource(id = R.dimen.primary_button)),
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = Color.Black
            ),
        ) {
            Text(text = name)
        }
    }
}