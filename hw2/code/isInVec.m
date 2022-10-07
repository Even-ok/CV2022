function   val = isInVec(val_i,mat_vec)
% Determine if val is in matvec
% return 1(is in) or 0(is not in).
[m,n] = size(mat_vec);
max_val =n;
val_tem =0;
for i=1:max_val
    if (val_i == mat_vec(1,i))
        val_tem = 1;
        break;
    end
end
val = val_tem;