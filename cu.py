import os

def rename_files(dir_path, old_str, new_str):
    for foldername, subfolders, filenames in os.walk(dir_path):
        for filename in filenames:
            if old_str in filename:
                src = os.path.join(foldername, filename)
                dst = os.path.join(foldername, filename.replace(old_str, new_str))
                os.rename(src, dst)
        for subfolder in subfolders:
            if old_str in subfolder:
                src = os.path.join(foldername, subfolder)
                dst = os.path.join(foldername, subfolder.replace(old_str, new_str))
                os.rename(src, dst)

# 调用函数，替换文件名
rename_files('D:\ss\HMCL-MOGAI\HMCL-javafx', 'hmcl', 'cu')
