package cn.thinkit.libtvqe;

public class TVQEJNI
{
	public native int  TVqeInit(int sampleRate, int aec_mode, int input_channel_num_voice, int input_channel_num_ref, int output_channel_num, String work_path_and_license_file_name);
	public native int  TVqeSetParam(String param_name, String param_value);
	public native int  TVqeProcess(short[] input_buf, short[] in_buf_ref, short[] output_buf, int aec_mode);
	public native int  TVqeFree();
	public native int  TVqeGetVersion(byte[] versionStr, short length);
}

