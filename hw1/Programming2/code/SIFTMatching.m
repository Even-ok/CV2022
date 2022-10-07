function matchArray = SIFTMatching(descriptor1, descriptor2, thresh)
% SIFTMatching: This function implements SIFT descriptor matching
%
% INPUT:
%   descriptor1: N1 * 128 matrix, each row is a SIFT descriptor.
%   descriptor2: N2 * 128 matrix.
%   thresh
% OUTPUT:
%   matchArray: N * 2 matrix, each row representing the match between the
%   descriptors(points).

    if ~exist('thresh', 'var'),
        thresh = 0.7;
    end

    matchArray = [];
    
    len1 = length(descriptor1);
    len2 = length(descriptor2);
    
for i = 1:len1
    % calculate the distance between descriptor in descriptor1 and every
    % one in descriptor2
	dist = sqrt(sum((repmat(descriptor1(i,:),len2,1) - descriptor2).^2,2));
	dist_sort = sort(dist);
	ratio = dist_sort(1)/dist_sort(2);
	if ratio < thresh
		index = find(dist==dist_sort(1));
		matchArray = [matchArray; [i, index]];
	end
end

end


    