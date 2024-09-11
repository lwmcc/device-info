package com.mccarty.currentdeviceinfo.di.module

import androidx.core.content.FileProvider
import com.mccarty.currentdeviceinfo.R

class AppFileProvider: FileProvider(R.xml.file_paths)