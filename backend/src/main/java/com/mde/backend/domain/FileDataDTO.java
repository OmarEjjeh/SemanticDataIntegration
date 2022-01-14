package com.mde.backend.domain;

public class FileDataDTO {
	private final FileData data;
	private final boolean success;
	
	
	
	
	public FileDataDTO(FileData data, boolean success) {
		super();
		this.data = data;
		this.success = success;
	}




	public FileData getData() {
		return data;
	}




	public boolean isSuccess() {
		return success;
	}




	public static class FileData{
		private final String file_name;
		private final String file_type;
		private final String creation_date;
		private final int file_size;
		public FileData(String file_name, String file_type, String creation_date, int file_size) {
			super();
			this.file_name = file_name;
			this.file_type = file_type;
			this.creation_date = creation_date;
			this.file_size = file_size;
		}
		public String getFile_name() {
			return file_name;
		}
		public String getFile_type() {
			return file_type;
		}
		public String getCreation_date() {
			return creation_date;
		}
		public int getFile_size() {
			return file_size;
		}

	}

	
}

