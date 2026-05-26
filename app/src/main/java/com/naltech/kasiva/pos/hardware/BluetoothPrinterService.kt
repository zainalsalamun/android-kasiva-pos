package com.naltech.kasiva.pos.hardware

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.util.*

class BluetoothPrinterService(private val context: Context) {
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var bluetoothSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null

    private val PRINTER_UUID: UUID = UUID.fromString("00001101-0000-0000-1000-800000805f9b")

    @SuppressLint("MissingPermission")
    suspend fun connectToDevice(deviceAddress: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val device: BluetoothDevice? = bluetoothAdapter?.getRemoteDevice(deviceAddress)
            bluetoothSocket = device?.createRfcommSocketToServiceRecord(PRINTER_UUID)
            bluetoothSocket?.connect()
            outputStream = bluetoothSocket?.outputStream
            true
        } catch (e: Exception) {
            Log.e("PrinterService", "Connection failed", e)
            false
        }
    }

    suspend fun printReceipt(content: String): Boolean = withContext(Dispatchers.IO) {
        try {
            outputStream?.let { stream ->
                // ESC/POS Init
                stream.write(byteArrayOf(0x1B, 0x40))
                // Write content
                stream.write(content.toByteArray())
                // Feed paper
                stream.write(byteArrayOf(0x0A, 0x0A, 0x0A))
                // Cut paper (some printers)
                stream.write(byteArrayOf(0x1D, 0x56, 0x41, 0x00))
                stream.flush()
                true
            } ?: false
        } catch (e: Exception) {
            Log.e("PrinterService", "Printing failed", e)
            false
        }
    }

    fun disconnect() {
        try {
            outputStream?.close()
            bluetoothSocket?.close()
        } catch (e: Exception) {
            Log.e("PrinterService", "Disconnect failed", e)
        }
    }
}
