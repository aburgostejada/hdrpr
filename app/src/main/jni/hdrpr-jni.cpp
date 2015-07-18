/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
#include <opencv2/opencv.hpp>
#include <opencv2/imgcodecs.hpp>
#include <opencv2/highgui.hpp>
#include <vector>
#include <iostream>
#include <fstream>
#include <string.h>
#include <jni.h>
#include <string>

using namespace cv;
using namespace std;

void loadExposureSeq(String, vector<Mat>&, vector<float>&, int maxHeight);
std::string ConvertJString(JNIEnv*, jstring);

extern "C" JNIEXPORT jstring JNICALL 
Java_ahxsoft_hdrpr_HDRProcessor_00024IncomingHandler_startProcessJNI( JNIEnv *env, jobject obj, jstring imagePath, jstring imageName, jint maxHeight, jstring toneMapAlg, jboolean hdrFile)
{
   vector<Mat> images;
   vector<float> times;
   std::string imgPath = ConvertJString( env, imagePath );
   std::string imgName = ConvertJString( env, imageName );
   std::string toneMapAlgType = ConvertJString( env, toneMapAlg );
   
   std::string drago = "Drago";
   std::string durand = "Durand";
   std::string autoMatic = "Auto";
   
   loadExposureSeq(imgPath, images, times, maxHeight);
   
   if(toneMapAlgType == autoMatic)
   {
        Mat fusion;
        Ptr<MergeMertens> merge_mertens = createMergeMertens();
        merge_mertens->process(images, fusion);
        std::string fileNameFusion = imgPath + imgName + ".png";
        imwrite(fileNameFusion, fusion * 255);
        fusion.release();
        merge_mertens.release();     
   }else {
        Mat response;
        Ptr<CalibrateDebevec> calibrate = createCalibrateDebevec();
        calibrate->process(images, response, times);
       
        Mat hdr;
       
        Ptr<MergeDebevec> merge_debevec = createMergeDebevec();
        merge_debevec->process(images, hdr, times, response);
       
        if(hdrFile){
            std::string fileNameHDR = imgPath + imgName + ".hdr";
            imwrite(fileNameHDR, hdr);     
        }
     
       response.release();
          
        if(toneMapAlgType == durand){
            Mat ldr_durand;
            Ptr<TonemapDurand> tonemap_du = createTonemapDurand(2.2f);
            tonemap_du->process(hdr, ldr_durand);
            std::string fileNameLDR_Durand = imgPath + imgName + ".png";
            imwrite(fileNameLDR_Durand, ldr_durand * 255);
            ldr_durand.release();
            tonemap_du.release();          
        }else if(toneMapAlgType == drago){
            Mat ldr_drago;
            Ptr<TonemapDrago> tonemap_dr = createTonemapDrago();
            tonemap_dr->process(hdr, ldr_drago);
            std::string fileNameLDR_Drago = imgPath + imgName +".png";
            imwrite(fileNameLDR_Drago, ldr_drago * 255);
            ldr_drago.release();
            tonemap_dr.release();
        }
   }
   
   return env->NewStringUTF(imgPath.c_str());
}

void loadExposureSeq(String path, vector<Mat>& images, vector<float>& times, int maxHeight)
{
    ifstream list_file((path + "p_file.txt").c_str());
    string name;
    float val;
    while(list_file >> name >> val) {
        Mat img = imread(path + name);
        cv::Size current_size = img.size();
        Mat dst;

        if(current_size.height <= maxHeight){
            cv::Size size(current_size.width,  current_size.height);
            resize(img, dst, size);
        }else{
            int width  = ((double) current_size.width / (double) current_size.height) * maxHeight;  
            cv::Size size(width,  maxHeight);
            resize(img, dst,size);
        }
        
        images.push_back(dst);
        times.push_back(val);
    }
    list_file.close();
}

std::string ConvertJString(JNIEnv* env, jstring str)
{
   const jsize len = env->GetStringUTFLength(str);
   const char* strChars = env->GetStringUTFChars(str, (jboolean *)0);

   std::string Result(strChars, len);

   env->ReleaseStringUTFChars(str, strChars);

   return Result;
}
