package com.tripor.article.model.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tripor.article.model.dto.ArticleDto;
import com.tripor.article.model.dto.ArticleListDto;
import com.tripor.article.model.dto.ArticlePostDto;
import com.tripor.article.model.dto.CommentDto;
import com.tripor.article.model.dto.FileInfoDto;
import com.tripor.article.model.mapper.ArticleMapper;
import com.tripor.article.model.mapper.CommentMapper;
import com.tripor.util.PageNavigation;

@Service
public class ArticleServiceImpl implements ArticleService {

	private final ArticleMapper articleMapper;
	private final CommentMapper commentMapper;

	@Autowired
	public ArticleServiceImpl(ArticleMapper articleMapper, CommentMapper commentMapper) {
		this.articleMapper = articleMapper;
		this.commentMapper = commentMapper;
	}



	@Override
	public int writeArticle(ArticlePostDto articlePostDto) throws Exception {
		ArticleDto articleDto = articlePostDto.getArticleDto();
		articleMapper.insert(articleDto);
		int aritcleId = articleMapper.lastKey();
		System.out.println("articleId : " + aritcleId);
		List<FileInfoDto> fileInfos = articlePostDto.getFileInfos();
		if (fileInfos != null && !fileInfos.isEmpty()) {
			articleDto.setFileInfos(fileInfos);
			articleMapper.registerFile(articleDto);
		}
		return aritcleId;
	}
	
	

	@Override
	public FileInfoDto registerImage(FileInfoDto image) throws Exception {
		articleMapper.registerImage(image);
		image.setImageId(articleMapper.lastKey());
		return image;
	}



	@Override
	public ArticleListDto listArticle(Map<String, String> map) throws Exception {
		int pgno = 1;
		String pg = (String) map.get("pgno");
		int currentPage = Integer.parseInt(map.get("pgno") == null ? "1" : map.get("pgno"));
		int sizePerPage = Integer.parseInt(map.get("spp") == null ? "20" : map.get("spp"));

		Map<String, Object> param = new HashMap<>();
		// 페이지 Navigation 관련
		int start = currentPage * sizePerPage - sizePerPage;
		
		param.put("pgno", Integer.parseInt(map.get("pgno")));
		param.put("spp", Integer.parseInt(map.get("spp")));
		param.put("nav", Integer.parseInt(map.get("nav")));
		
		param.put("start", start);
		param.put("listSize", sizePerPage);

		// 검색 관련
		param.put("word", map.get("word") == null ? "" : map.get("word"));
		String key = map.get("key");
		param.put("key", key == null ? "" : key);
//		if ("member_id".equals(key))
//			param.put("key", key == null ? "" : "article.member_id");

		List<ArticleDto> list = articleMapper.findAll(param);

//		if (map.containsKey("pgno") && pg != null && pg.length() > 0)
//			pgno = Integer.parseInt(pg);
//		System.out.println(pgno);
//		int listSize = BoardSize.LIST.getBoardSize();
//		int start = (pgno - 1) * listSize;
//		map.put("start", start);
//		map.put("listSize", listSize);
//		System.out.println(map);

		ArticleListDto articleListDto = new ArticleListDto();
		PageNavigation pageNavigation = makePageNavigation(param);
		System.out.println(pageNavigation);
		
		articleListDto.setArticles(list);
		articleListDto.setPageNavigation(pageNavigation);

		return articleListDto;
	}

	@Override
	public ArticleDto getArticle(int articleId) throws Exception {
		return articleMapper.findById(articleId);
	}

	@Override
	public PageNavigation makePageNavigation(Map<String, Object> map) throws Exception {
		PageNavigation pageNavigation = new PageNavigation();
		int currentPage = (int) map.get("pgno");
		int naviSize = (int) map.get("nav");
		int listSize = (int) map.get("spp");
		pageNavigation.setCurrentPage(currentPage);
		pageNavigation.setNaviSize(naviSize);
		int totalCount = articleMapper.getArticleCount(map);
		pageNavigation.setTotalCount(totalCount);
		int totalPageCount = (totalCount - 1) / listSize + 1;
		pageNavigation.setTotalPageCount(totalPageCount);
		return pageNavigation;
	}

	
	
	@Override
	public void deleteImage(int imageId) throws Exception {
		articleMapper.deleteImageByImageId(imageId);
	}



	@Override
	public void updateHit(int articleId) throws Exception {
		articleMapper.increaseHit(articleId);
	}

	@Override
	public void modifyArticle(ArticleDto articleDto) throws Exception {
		articleMapper.update(articleDto);
		List<FileInfoDto> fileInfos = articleDto.getFileInfos();
		
		System.out.println(articleDto.getArticleId());
		articleMapper.deleteRelationByArticleId(articleDto.getArticleId());
		if(!fileInfos.isEmpty())
			articleMapper.registerFile(articleDto);
	}

	@Override
	public void deleteArticle(int articleId) throws Exception {
		articleMapper.deleteRelationByArticleId(articleId);
		List<Integer> imageIds = articleMapper.getRelationImageIdsByArticleId(articleId);
		for(Integer imageId : imageIds) {
			articleMapper.deleteImageByImageId(imageId);
		}
		articleMapper.delete(articleId);
	}



	@Override
	public void addComment(CommentDto commentDto) throws Exception {
		 commentMapper.insertComment(commentDto);
	}



	@Override
	public List<CommentDto> getCommentsByArticleId(int articleId) throws Exception {
		return commentMapper.findByArticleId(articleId);
	}



	@Override
	public void updateComment(CommentDto commentDto) throws Exception {
		commentMapper.updateComment(commentDto);
	}



	@Override
	public void deleteComment(int commentId) throws Exception {
		commentMapper.deleteChildComment(commentId);
		commentMapper.deleteComment(commentId);
	}
}
